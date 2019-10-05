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
import io.github.alphajiang.hyena.biz.calculator.CostCalculator;
import io.github.alphajiang.hyena.biz.calculator.PointRecCalculator;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private PointRecCalculator pointRecCalculator;

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
    public void processPoint(PointUsage usage, PointCache pointCache) {
        PointPo curPoint = pointCache.getPoint();
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.FREEZE, usage, curPoint);

        List<PointRecLogPo> recLogs = new ArrayList<>();

        var recLogsRet = this.freezeCostLoop(usage.getType(), pointCache, pointLog, usage.getPoint());

        recLogs.addAll(recLogsRet.getRecLogs());

        curPoint.setFrozen(curPoint.getFrozen() + recLogsRet.getDelta())
                .setAvailable(curPoint.getAvailable() - recLogsRet.getDelta())
                .setFrozenCost(curPoint.getFrozenCost() + recLogsRet.getDeltaCost());
        point2Update.setFrozen(curPoint.getFrozen())
                .setAvailable(curPoint.getAvailable())
                .setFrozenCost(curPoint.getFrozenCost());

        pointLog = this.pointLogDs.buildPointLog(PointOpType.FREEZE, usage, curPoint);

        pointLog.setDelta(recLogsRet.getDelta())
                .setDeltaCost(recLogsRet.getDeltaCost());

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, recLogs);
    }


    private LoopResult freezeCostLoop(String type, PointCache pointCache,
                                      PointLogPo pointLog, long expected) {
        log.info("freezeCost. type = {}, uid = {}, expected = {}",
                type, pointCache.getPoint().getUid(), expected);
        LoopResult result = new LoopResult();
        long sum = 0L;
        long sumPoint = 0L;
        long cost = 0L;
        //long deltaCost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
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
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                long deltaCost = gap;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sumPoint).setDeltaCost(cost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }


}
