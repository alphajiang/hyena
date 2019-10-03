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
import io.github.alphajiang.hyena.biz.point.CostCalculator;
import io.github.alphajiang.hyena.biz.point.PointCache;
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
public class PointFreezeCostStrategy extends AbstractPointStrategy {

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

    @Autowired
    private CostCalculator costCalculator;


    @Override
    public CalcType getType() {
        return CalcType.FREEZE_COST;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        log.info("cost freeze. usage = {}", usage);
        super.preProcess(usage, false);

        PointPo curPoint = null;

        curPoint = this.freezeCost(usage);

        if (curPoint == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        return curPoint;
    }

    @Override
    public void processPoint(PointUsage usage, PointCache pointCache){

    }

    private PointPo freezeCost(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setFrozenCost(curPoint.getFrozenCost() + usage.getPoint());
        var point2Update = new PointPo();
        point2Update.setFrozenCost(curPoint.getFrozenCost())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        curPoint.setSeqNum(curPoint.getSeqNum() + 1);

        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.FREEZE_COST, usage, curPoint);
        long gap = usage.getPoint();
        long cost = 0L;
        long sumPoint = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.freezePointLoop(usage.getType(), curPoint, pointLog, gap);
                gap = gap - recLogsRet.getDeltaCost();
                cost = cost + recLogsRet.getDeltaCost();
                sumPoint += recLogsRet.getDelta();
                recLogs.addAll(recLogsRet.getRecLogs());
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if (gap != 0L) {
            log.warn("no enough available cost! gap = {}", gap);
            //throw new HyenaServiceException("no enough available point!");
        }
        curPoint.setFrozen(curPoint.getFrozen() + sumPoint)
                .setAvailable(curPoint.getAvailable() - sumPoint);
        point2Update.setFrozen(curPoint.getFrozen())
                .setAvailable(curPoint.getAvailable());

        pointLog.setDelta(sumPoint)
                .setDeltaCost(cost).setFrozenCost(curPoint.getFrozenCost())
                .setAvailable(curPoint.getAvailable())
                .setFrozen(curPoint.getFrozen());
        //HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");
        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if (!ret) {
            log.warn("freeze cost failed!!! please retry later. usage = {}", usage);
            return null;
        }

        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        return curPoint;
    }


    private LoopResult freezePointLoop(String type, PointPo point, PointLogPo pointLog, long expected) {
        log.info("freeze. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
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
        long sumPoint = 0L;
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = expected - sum;
            long availableCost = this.costCalculator.getAvailableCost(rec);
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (availableCost < gap) {
                sum += availableCost;
                long deltaCost = availableCost;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var retRec = this.pointRecDs.freezePoint(type, rec, delta, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = gap;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var retRec = this.pointRecDs.freezePoint(type, rec, delta, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sumPoint).setDeltaCost(cost).setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }


}
