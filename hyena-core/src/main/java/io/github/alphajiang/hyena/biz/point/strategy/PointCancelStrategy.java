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
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PointCancelStrategy extends AbstractPointStrategy {


    @Autowired
    private PointLogDs pointLogDs;


    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;


    @Autowired
    private PointRecCalculator pointRecCalculator;

    @Autowired
    private PointBuilder pointBuilder;

    @Autowired
    private CostCalculator costCalculator;

    @Override
    public CalcType getType() {
        return CalcType.CANCEL;
    }

    //    @Override
//    @Transactional
//    public PointPo process(PointUsage usage) {
//
//
//        return curPoint;
//    }
    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {

        if (usage.getRecId() != null && usage.getRecId().longValue() > 0L) {
            cancelByRecId(usage, pointCache);
        } else {
            cancelPoint(usage, pointCache);
        }
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(pointCache.getPoint(), ret);
        return ret;
    }

    private void cancelByRecId(PointUsage usage, PointCache pointCache) {
        HyenaAssert.notNull(usage.getRecId(), "invalid parameter, 'recId' can't be null");
        HyenaAssert.isTrue(usage.getRecId().longValue() > 0L, "invalid parameter: recId");
        PointPo curPoint = pointCache.getPoint();
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point");

        Optional<PointRecPo> recOpt = pointCache.getPoint().getRecList().stream().filter(r -> r.getId() == usage.getRecId()).findFirst();
        if (recOpt.isEmpty()) {
            log.warn("rec not found with recId = {}", usage.getRecId());
            return;
        }
        PointRecPo rec = recOpt.get();
        if (!rec.getEnable()) {
            return;
        }
        log.info("curPoint = {}", curPoint);
        log.info("rec = {}", rec);
        HyenaAssert.isTrue(rec.getFrozen().longValue() < 1L,
                HyenaConstants.RES_CODE_STATUS_ERROR,
                "can't cancel frozen point record");
        HyenaAssert.isTrue(rec.getPid() == curPoint.getId(), "invalid parameter.");
        HyenaAssert.isTrue(rec.getAvailable().longValue() == usage.getPoint(), "point mis-match");
        long delta = rec.getAvailable();
        long deltaCost = this.costCalculator.accountCost(rec, delta);

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setPoint(curPoint.getPoint() - delta)
                .setAvailable(curPoint.getAvailable() - delta);
        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        rec.setAvailable(0L).setCancelled(rec.getCancelled() + delta);
        if (rec.getFrozen() < 1L) {
            rec.setEnable(false);
            pointCache.getPoint().setRecList(pointCache.getPoint().getRecList().stream()
                    .filter(r -> r.getEnable()).collect(Collectors.toList()));

        }

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.CANCEL, usage, curPoint);
        PointRecLogPo recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, deltaCost);

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), List.of(rec));
        pointFlowService.addFlow(usage, pointLog, List.of(recLog));
    }

    private void cancelPoint(PointUsage usage, PointCache pointCache) {

        PointPo curPoint = pointCache.getPoint();
        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point");

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.CANCEL, usage, curPoint);

        LoopResult lr = this.cancelPointLoop(usage.getType(), pointCache, pointLog, usage.getPoint());

        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), lr.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, lr.getRecLogs());


    }


    private LoopResult cancelPointLoop(String type, PointCache pointCache,
                                       PointLogPo pointLog, long expected) {
        log.info("cancel. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);

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
            } else if (rec.getAvailable() < 1L) {
                // do nothing
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                PointRecCalcResult calcResult = this.pointRecCalculator.cancelPoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum += gap;
                PointRecCalcResult calcResult = this.pointRecCalculator.cancelPoint(rec, gap);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, gap, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }
        pointCache.getPoint().setRecList(pointCache.getPoint().getRecList().stream().filter(rec -> rec.getEnable()).collect(Collectors.toList()));

        result.setDelta(sum).setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }
}
