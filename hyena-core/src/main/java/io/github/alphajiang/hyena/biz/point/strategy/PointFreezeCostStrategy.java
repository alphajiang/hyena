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
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.StringUtils;
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
//        HyenaAssert.notNull(usage.getCost(), "invalid parameter: cost");
        BigDecimal availableCost = curPoint.getCost().subtract(curPoint.getFrozenCost());
        HyenaAssert.isTrue(DecimalUtils.gte(availableCost, usage.getCost()),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        if (usage.getPoint() == null || DecimalUtils.lt(usage.getPoint(), usage.getCost())) {
            usage.setPoint(usage.getCost());
        }
        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.FREEZE, usage, curPoint);

        List<PointRecLogDto> recLogs = new ArrayList<>();

        var recLogsRet = this.freezeCostLoop(usage.getType(), pointCache, usage, pointLog, usage.getCost());
        if (DecimalUtils.gt(usage.getPoint(), usage.getCost())) {
            // 需要追加冻结赠送金
            BigDecimal freePoint = usage.getPoint().subtract(usage.getCost());
            LoopResult recLogRetFree = this.freezeFreePoint(usage.getType(), usage, pointCache, pointLog, freePoint);
            recLogsRet.setDelta(recLogsRet.getDelta().add(recLogRetFree.getDelta()));
            recLogsRet.getRecList4Update().addAll(recLogRetFree.getRecList4Update());
            recLogsRet.getRecLogs().addAll(recLogRetFree.getRecLogs());
            recLogsRet.getForList().addAll(recLogRetFree.getForList());
        }
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
        pointFlowService.addFreezeOrderRec(usage.getType(), recLogsRet.getForList());
        pointFlowService.addFlow(usage, pointLog, recLogs);

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        return ret;
    }


    private LoopResult freezeCostLoop(String type, PointCache pointCache,
                                      PointUsage usage,
                                      PointLogPo pointLog, BigDecimal expected) {
        log.info("freezeCost. type = {}, uid = {}, expected = {}",
                type, pointCache.getPoint().getUid(), expected);
        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal sumPoint = DecimalUtils.ZERO;
        BigDecimal cost = DecimalUtils.ZERO;
        //long deltaCost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<FreezeOrderRecPo> forList = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            if (StringUtils.isNotBlank(usage.getRecOrderNo())
                    && !usage.getRecOrderNo().equals(rec.getOrderNo())) {
                continue;
            }
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
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), delta, calcResult.getDeltaCost());
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                BigDecimal deltaCost = gap;
                BigDecimal delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint = sumPoint.add(delta);
                cost = cost.add(deltaCost);
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), gap, calcResult.getDeltaCost());
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sumPoint).setDeltaCost(cost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs)
                .setForList(forList);
        log.debug("result = {}", result);
        return result;
    }

    private LoopResult freezeFreePoint(String type, PointUsage usage, PointCache pointCache,
                                       PointLogPo pointLog, BigDecimal expected) {
        log.info("freezeFreePoint. type = {}, uid = {}, expected = {}",
                type, pointCache.getPoint().getUid(), expected);
        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal deltaCost = DecimalUtils.ZERO;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        List<FreezeOrderRecPo> forList = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            BigDecimal gap = expected.subtract(sum);
            if (StringUtils.isNotBlank(usage.getRecOrderNo())
                    && !usage.getRecOrderNo().equals(rec.getOrderNo())) {
                continue;
            } else if (DecimalUtils.gt(rec.getTotalCost(), BigDecimal.ZERO)) {
                // 忽略有实际金额的资金块
                continue;
            }
            if (DecimalUtils.lte(gap, DecimalUtils.ZERO)) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (DecimalUtils.lte(rec.getAvailable(), DecimalUtils.ZERO)) {
                // do nothing
            } else if (DecimalUtils.lt(rec.getAvailable(), gap)) {
                sum = sum.add(rec.getAvailable());
                BigDecimal delta = rec.getAvailable();
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), delta, calcResult.getDeltaCost());
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum = sum.add(gap);
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, gap);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), gap, calcResult.getDeltaCost());
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, gap, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }
        result.setDelta(sum).setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs)
                .setForList(forList);
        log.debug("result = {}", result);
        return result;


    }

}
