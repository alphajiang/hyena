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
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.po.*;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private PointMemCacheService pointMemCacheService;

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

        if (curPoint.getFrozen() < usage.getPoint()) {
            log.warn("no enough frozen point. usage = {}, curPoint = {}", usage, curPoint);
            throw new HyenaNoPointException("no enough frozen point", Level.WARN);
        }
        HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setAvailable(curPoint.getAvailable() + usage.getPoint())
                .setFrozen(curPoint.getFrozen() - usage.getPoint());

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

        if (recLogsRet.getDeltaCost() > 0L) {
            pointLog.setDeltaCost(recLogsRet.getDeltaCost())
                    .setFrozenCost(pointLog.getFrozenCost() - recLogsRet.getDeltaCost());
            curPoint.setFrozenCost(curPoint.getFrozenCost() - recLogsRet.getDeltaCost());
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        if (CollectionUtils.isNotEmpty(recLogsRet.getForList())) {
            pointFlowService.closeFreezeOrderRec(usage.getType(), recLogsRet.getForList());
        }
        pointFlowService.addFlow(usage, pointLog, recLogsRet.getRecLogs());

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        return ret;
    }


    private LoopResult unfreezePointLoop(PointUsage usage, PointCache pointCache,
                                         PointLogPo pointLog, long expected) {
        log.info("unfreeze. type = {}, uid = {}, expected = {}",
                usage.getType(), pointCache.getPoint().getUid(), expected);
        //List<PointRecPo> recList = pointCache.getPoint().getRecList();
        LoopResult result = new LoopResult();
        long sum = 0L;
        long deltaCost = 0L;

        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen() < 1L) {
                // do nothing
            } else if (rec.getFrozen() < gap) {
                sum += rec.getFrozen();
                long delta = rec.getFrozen();

                PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, delta, null);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum += gap;
                PointRecCalcResult calcResult = this.pointRecCalculator.unfreezePoint(rec, gap, null);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
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
        log.info("unfreeze. type = {}, uid = {}",
                usage.getType(), pointCache.getPoint().getUid());
        if (StringUtils.isBlank(usage.getOrderNo())) {
            log.warn("invalid parameter: orderNo is blank. usage = {}", usage);
            throw new HyenaParameterException("invalid parameter, orderNo must not blank");
        }
        List<FreezeOrderRecPo> forList = this.freezeOrderRecDs.getFreezeOrderRecList(usage.getType(),
                pointCache.getPoint().getId(),
                usage.getOrderType(), usage.getOrderNo());
        long frozen = forList.stream().mapToLong(FreezeOrderRecPo::getFrozen).sum();
        if (frozen != usage.getPoint()) {
            log.warn("frozen number mis-match. type = {}, uid = {}, usage.frozen = {}, actual frozen = {}",
                    usage.getType(), usage.getUid(), usage.getPoint(), frozen);
            throw new HyenaNoPointException("frozen number mis-match", Level.WARN);
        }


        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();

        // 将同一个积分块的冻结记录做合并
        Map<Long, FreezeOrderRecPo> forMap = new HashMap<>();
        forList.stream().forEach(fo -> {
            if (forMap.containsKey(fo.getRecId())) {
                FreezeOrderRecPo l = forMap.get(fo.getRecId());
                l.setFrozen(l.getFrozen() + fo.getFrozen())
                        .setFrozenCost(l.getFrozenCost() + fo.getFrozenCost());
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
                        PointRecLogPo recLog = this.pointBuilder.buildRecLog(rec, pointLog, f.getFrozen(), f.getFrozenCost());
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

        long delta = forList.stream().mapToLong(FreezeOrderRecPo::getFrozen).sum();
        long deltaCost = forList.stream().mapToLong(FreezeOrderRecPo::getFrozenCost).sum();

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
