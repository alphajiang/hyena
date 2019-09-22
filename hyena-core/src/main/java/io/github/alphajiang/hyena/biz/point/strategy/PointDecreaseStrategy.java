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
public class PointDecreaseStrategy extends AbstractPointStrategy {

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
        return CalcType.DECREASE;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        log.info("decrease. usage = {}", usage);
        super.preProcess(usage);
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        log.debug("curPoint = {}", curPoint);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
//        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= usage.getPoint(),
//                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
//                "no enough available point. current available point is " + curPoint.getAvailable(),
//                Level.WARN,
//                HyenaNoPointException.class);



        curPoint.setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setUsed(curPoint.getUsed() + usage.getPoint());
//        if(curPoint.getFrozen() < 0L) {
//            // 使用可用余额来抵扣超扣部分
//            curPoint.setAvailable(curPoint.getAvailable() + curPoint.getFrozen());
//            curPoint.setFrozen(0L);
//        }
        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setUsed(curPoint.getUsed()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());
        curPoint.setSeqNum(curPoint.getSeqNum() + 1);

        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.DECREASE, usage, curPoint);

        long gap = usage.getPoint();
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.decreasePointLoop(usage.getType(), curPoint, pointLog, gap);
                gap = gap - recLogsRet.getDelta();
                cost = cost + recLogsRet.getDeltaCost();
                recLogs.addAll(recLogsRet.getRecLogs());
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if (cost > 0L) {
            pointLog.setDeltaCost(cost).setCost(pointLog.getCost() - cost);
            curPoint.setCost(curPoint.getCost() - cost);
            point2Update.setCost(curPoint.getCost());
        }

        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");

        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        return curPoint;
    }

    private LoopResult decreasePointLoop(String type, PointPo point, PointLogPo pointLog, long expected) {
        log.info("decrease. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(point.getUid()).setAvailable(true).setLock(true)
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
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                long costDelta = this.pointRecDs.accountCost(rec, delta);
                cost += costDelta;
                var retRec = this.pointRecDs.decreasePoint(type, rec, gap, costDelta);


                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, costDelta);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long costDelta = this.pointRecDs.accountCost(rec, gap);
                cost += costDelta;
                var retRec = this.pointRecDs.decreasePoint(type, rec, gap, costDelta);

                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, gap, costDelta);
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sum).setDeltaCost(cost).setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }


}
