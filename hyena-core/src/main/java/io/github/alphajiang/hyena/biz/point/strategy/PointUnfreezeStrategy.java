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
import io.github.alphajiang.hyena.ds.service.FreezeOrderRecDs;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PointUnfreezeStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecCalculator pointRecCalculator;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private FreezeOrderRecDs freezeOrderRecDs;

    @Autowired
    private PointFlowService pointFlowService;

//    @Autowired
//    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private CostCalculator costCalculator;

    @Autowired
    private PointBuilder pointBuilder;

    @Override
    public CalcType getType() {
        return CalcType.UNFREEZE;
    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        PointPo curPoint = pointCache.getPoint();

        if (curPoint.getFrozen().compareTo(usage.getPoint()) < 0) {
            log.warn("no enough frozen point. usage = {}, curPoint = {}", usage, curPoint);
            throw new HyenaNoPointException("no enough frozen point", Level.WARN);
        }
        HyenaAssert.isTrue(curPoint.getFrozen().compareTo(usage.getPoint()) >= 0,
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setAvailable(curPoint.getAvailable().add(usage.getPoint()))
                .setFrozen(curPoint.getFrozen().subtract(usage.getPoint()));

        var point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable())
                .setFrozen(curPoint.getFrozen())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.UNFREEZE, usage, curPoint);

        LoopResult recLogsRet = null;
        if (Boolean.TRUE.equals(usage.getUnfreezeByOrderNo())) {
            recLogsRet = this.unfreezeByOrderNo(usage,
                    pointCache, pointLog);
        } else {
            recLogsRet = this.unfreezePointLoop(usage,
                    pointCache, pointLog, usage.getPoint());
        }

        if (recLogsRet.getDeltaCost().compareTo(DecimalUtils.ZERO) > 0) {
            pointLog.setDeltaCost(recLogsRet.getDeltaCost())
                    .setFrozenCost(pointLog.getFrozenCost().subtract(recLogsRet.getDeltaCost()));
            curPoint.setFrozenCost(curPoint.getFrozenCost().subtract(recLogsRet.getDeltaCost()));
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost())
                .setLogs(List.of(PointLogDto.build(pointLog)));
        if (usage.isDoUpdate()) {
            pointFlowService.updatePoint(usage.getType(), point2Update);
            pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
            if (CollectionUtils.isNotEmpty(recLogsRet.getForList())) {
                pointFlowService.closeFreezeOrderRec(usage.getType(), recLogsRet.getForList());
            }
            pointFlowService.addFlow(usage, pointLog, recLogsRet.getRecLogs());

        } else {
            ret.getUpdateQ().setPoint(point2Update);
            ret.getUpdateQ().getLogs().add(pointLog);
            ret.getUpdateQ().getRecList().addAll(recLogsRet.getRecList4Update());
            if (CollectionUtils.isNotEmpty(recLogsRet.getForList())) {
                ret.getUpdateQ().getFoList().addAll(recLogsRet.getForList());
            }
            ret.getUpdateQ().getRecLogs().addAll(recLogsRet.getRecLogs());
        }
        return ret;
    }


    private LoopResult unfreezePointLoop(PointUsage usage, PointCache pointCache,
                                         PointLogPo pointLog, BigDecimal expected) {
        log.info("unfreeze. type = {}, uid = {}, expected = {}",
                usage.getType(), pointCache.getPoint().getUid(), expected);
        //List<PointRecPo> recList = pointCache.getPoint().getRecList();
        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal deltaCost = DecimalUtils.ZERO;

        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            BigDecimal gap = expected.subtract(sum);
            if (gap.compareTo(DecimalUtils.ZERO) < 1) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen().compareTo(DecimalUtils.ZERO) < 1) {
                // do nothing
            } else if (rec.getFrozen().compareTo(gap) < 1) {
                sum = sum.add(rec.getFrozen());
                BigDecimal delta = rec.getFrozen();

                PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, delta, null);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum = sum.add(gap);
                PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, gap, null);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, gap, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }

        result.setDelta(sum).setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }

    private LoopResult unfreezeByOrderNo(PointUsage usage, PointCache pointCache,
                                         PointLogPo pointLog) {
        log.info("unfreezeByOrderNo. type = {}, uid = {}",
                usage.getType(), pointCache.getPoint().getUid());
        if (StringUtils.isBlank(usage.getOrderNo())) {
            log.warn("invalid parameter: orderNo is blank. usage = {}", usage);
            throw new HyenaParameterException("invalid parameter, orderNo must not blank");
        }
        List<FreezeOrderRecPo> forList = this.freezeOrderRecDs.getFreezeOrderRecList(usage.getType(),
                pointCache.getPoint().getId(),
                usage.getOrderType(), usage.getOrderNo());
        pointCache.getPoint().addForList(forList);
        if (pointCache.getPoint().getForList() == null || pointCache.getPoint().getForList().isEmpty()) {
            forList = List.of();
        } else {
            forList = pointCache.getPoint().getForList()
                    .values().stream()
                    .filter(o -> StringUtils.equals(o.getOrderNo(), usage.getOrderNo()))
                    .collect(Collectors.toList());
        }
        BigDecimal frozen = forList.stream().map(FreezeOrderRecPo::getFrozen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //long frozen = forList.stream().mapToLong(FreezeOrderRecPo::getFrozen).sum();
        if (frozen.compareTo(usage.getPoint()) != 0) {
            log.warn("frozen number mis-match. type = {}, uid = {}, usage.frozen = {}, actual frozen = {}",
                    usage.getType(), usage.getUid(), usage.getPoint(), frozen);
            throw new HyenaNoPointException("frozen number mis-match", Level.WARN);
        }


        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();

        // 将同一个积分块的冻结记录做合并
        Map<Long, FreezeOrderRecPo> forMap = new HashMap<>();
        forList.stream().forEach(fo -> {
            if (forMap.containsKey(fo.getRecId())) {
                FreezeOrderRecPo l = forMap.get(fo.getRecId());
                l.setFrozen(l.getFrozen().add(fo.getFrozen()))
                        .setFrozenCost(l.getFrozenCost().add(fo.getFrozenCost()));
            } else {
                FreezeOrderRecPo l = new FreezeOrderRecPo();
                BeanUtils.copyProperties(fo, l);
                forMap.put(fo.getRecId(), l);
            }
        });

        forMap.forEach((k, f) -> {
            pointCache.getPoint().getRecList()
                    .stream().filter(rec -> rec.getId() == f.getRecId())
                    .findFirst()
                    .ifPresent(rec -> {
                        PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, f.getFrozen(), f.getFrozenCost());
                        recList4Update.add(calcResult.getRec4Update());
                        var recLog = this.pointBuilder.buildRecLog(rec, pointLog, f.getFrozen(), f.getFrozenCost());
                        recLogs.add(recLog);
                    });
        });
//        forList.stream().forEach(f -> {
//            pointCache.getPoint().getRecList()
//                    .stream().filter(rec -> rec.getId() == f.getRecId())
//                    .findFirst()
//                    .ifPresent(rec -> {
//                        PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, f.getFrozen(), f.getFrozenCost());
//                        recList4Update.add(calcResult.getRec4Update());
//                        PointRecLogPo recLog = this.pointBuilder.buildRecLog(rec, pointLog, f.getFrozen(), f.getFrozenCost());
//                        recLogs.add(recLog);
//                    });
//        });

        BigDecimal delta = DecimalUtils.ZERO;
        BigDecimal deltaCost = DecimalUtils.ZERO;
        for (var fr : forList) {
            delta = delta.add(fr.getFrozen());
            deltaCost = deltaCost.add(fr.getFrozenCost());
        }

        LoopResult result = new LoopResult();

        result.setDelta(delta)
                .setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs)
                .setForList(forList);
        log.debug("result = {}", result);
        return result;
    }
}
