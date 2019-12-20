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
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    @Autowired
    private PointBuilder pointBuilder;

    @Override
    public CalcType getType() {
        return CalcType.FREEZE_COST;
    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        PointPo curPoint = pointCache.getPoint();
        HyenaAssert.notNull(usage.getCost(), "invalid parameter: cost");
        BigDecimal availableCost = curPoint.getCost().subtract(curPoint.getFrozenCost());
        HyenaAssert.isTrue(DecimalUtils.gte(availableCost, usage.getCost()),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.FREEZE, usage, curPoint);

        List<PointRecLogDto> recLogs = new ArrayList<>();

        var recLogsRet = this.freezeCostLoop(usage.getType(), pointCache, pointLog, usage.getCost());

        recLogs.addAll(recLogsRet.getRecLogs());

        curPoint.setFrozen(curPoint.getFrozen().add(recLogsRet.getDelta()))
                .setAvailable(curPoint.getAvailable().subtract(recLogsRet.getDelta()))
                .setFrozenCost(curPoint.getFrozenCost().add(recLogsRet.getDeltaCost()));
        point2Update.setFrozen(curPoint.getFrozen())
                .setAvailable(curPoint.getAvailable())
                .setFrozenCost(curPoint.getFrozenCost());

        pointLog = this.pointBuilder.buildPointLog(PointOpType.FREEZE, usage, curPoint);

        pointLog.setDelta(recLogsRet.getDelta())
                .setDeltaCost(recLogsRet.getDeltaCost());

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, recLogs);

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        return ret;
    }


    private LoopResult freezeCostLoop(String type, PointCache pointCache,
                                      PointLogPo pointLog, BigDecimal expected) {
        log.info("freezeCost. type = {}, uid = {}, expected = {}",
                type, pointCache.getPoint().getUid(), expected);
        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal sumPoint = DecimalUtils.ZERO;
        BigDecimal cost = DecimalUtils.ZERO;
        //long deltaCost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            BigDecimal gap = expected.subtract(sum);
            BigDecimal availableCost = this.costCalculator.getAvailableCost(rec);
            if (DecimalUtils.lte(gap, DecimalUtils.ZERO)) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (DecimalUtils.lt(availableCost, gap)) {
                sum = sum.add(availableCost);
                BigDecimal deltaCost = availableCost;
                BigDecimal delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint = sumPoint.add(delta);
                cost = cost.add(deltaCost);
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                BigDecimal deltaCost = gap;
                BigDecimal delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint = sumPoint.add(delta);
                cost = cost.add(deltaCost);
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
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
