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
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private PointMemCacheService pointMemCacheService;


    @Autowired
    private CostCalculator costCalculator;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE;
    }

    @Override
    public void processPoint(PointUsage usage, PointCache pointCache) {
        //PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        PointPo curPoint = pointCache.getPoint();
        log.debug("curPoint = {}", curPoint);
//        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
//                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);


        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setUsed(curPoint.getUsed() + usage.getPoint());
        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setUsed(curPoint.getUsed()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.DECREASE, usage, curPoint);

        long gap = usage.getPoint();
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();

        var recLogsRet = this.decreasePointLoop(usage.getType(),
                pointCache,
                pointLog, gap);
        gap = gap - recLogsRet.getDelta();
        cost = cost + recLogsRet.getDeltaCost();
        recLogs.addAll(recLogsRet.getRecLogs());
        log.debug("gap = {}", gap);

        if (cost > 0L) {
            pointLog.setDeltaCost(cost).setCost(pointLog.getCost() - cost);
            curPoint.setCost(curPoint.getCost() - cost);
            point2Update.setCost(curPoint.getCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        //return curPoint;
    }

    private LoopResult decreasePointLoop(String type, PointCache pointCache,
                                         PointLogPo pointLog, long expected) {
        log.info("decrease. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);

        LoopResult result = new LoopResult();
        long sum = 0L;
        long cost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getAvailable() < 1L) {
                // do nothing
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                long costDelta = this.costCalculator.accountCost(rec, delta);
                cost += costDelta;
                var rec4Update = this.pointRecDs.decreasePoint(type, rec, gap, costDelta);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, costDelta);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long costDelta = this.costCalculator.accountCost(rec, gap);
                cost += costDelta;
                var rec4Update = this.pointRecDs.decreasePoint(type, rec, gap, costDelta);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, gap, costDelta);
                recLogs.add(recLog);
                break;
            }
        }
        pointCache.getPoint().setRecList(pointCache.getPoint().getRecList().stream().filter(rec -> rec.getEnable()).collect(Collectors.toList()));
        //var ret = point - sum;
        result.setDelta(sum).setDeltaCost(cost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }


}
