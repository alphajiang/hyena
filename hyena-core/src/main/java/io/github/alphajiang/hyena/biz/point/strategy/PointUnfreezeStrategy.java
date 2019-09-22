/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PointUnfreezeStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.UNFREEZE;
    }

    @Override
    @Transactional//(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        log.info("unfreeze. usage = {}", usage);
        super.preProcess(usage);
//        int retry = 3;
        PointPo curPoint = this.unfreeze(usage);

        if (curPoint == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        return curPoint;
    }



    private PointPo unfreeze(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");


        curPoint.setAvailable(curPoint.getAvailable() + usage.getPoint())
                .setFrozen(curPoint.getFrozen() - usage.getPoint());

        var point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable()).setFrozen(curPoint.getFrozen())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        curPoint.setSeqNum(curPoint.getSeqNum() + 1);

        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.UNFREEZE, usage, curPoint);

        long gap = usage.getPoint();
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.unfreezePointLoop(usage.getType(), curPoint, pointLog, gap);
                gap = gap - recLogsRet.getDelta();
                cost += recLogsRet.getDeltaCost();
                recLogs.addAll(recLogsRet.getRecLogs());
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if(gap != 0L) {
            log.warn("no enough frozen point. usage = {}, point = {}", usage, curPoint);
        }
//        if (CollectionUtils.isNotEmpty(recLogs)) {
//            this.pointRecLogDs.addPointRecLogs(usage.getType(), recLogs);
//        }

        if (cost > 0L) {
            pointLog.setDeltaCost(cost).setFrozenCost(pointLog.getFrozenCost() - cost);
            curPoint.setFrozenCost(curPoint.getFrozenCost() - cost);
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }
        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if (!ret) {
            log.warn("unfreeze failed!!! please retry later. usage = {}", usage);
            return null;
        }


        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        //pointFlowService.addFlow(getType(), usage, curPoint);
        return curPoint;
    }


    private LoopResult unfreezePointLoop(String type, PointPo point, PointLogPo pointLog, long expected) {
        log.info("unfreeze. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(point.getUid()).setFrozen(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecDs.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        LoopResult result = new LoopResult();
        long sum = 0L;
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen() < gap) {
                sum += rec.getFrozen();
                long delta = rec.getFrozen();
                long deltaCost = this.pointRecDs.accountCost4Unfreeze(rec, delta);
                cost += deltaCost;
                var retRec = this.pointRecDs.unfreezePoint(type, rec, gap, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = this.pointRecDs.accountCost4Unfreeze(rec, gap);
                cost += deltaCost;
                var retRec = this.pointRecDs.unfreezePoint(type, rec, gap, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, gap, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }

        result.setDelta(sum).setDeltaCost(cost).setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }
}
