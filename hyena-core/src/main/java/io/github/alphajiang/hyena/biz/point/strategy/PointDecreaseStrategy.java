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
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    private PointRecCalculator pointRecCalculator;


    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;

//    @Autowired
//    private HyenaCacheFactory hyenaCacheFactory;


    @Autowired
    private PointBuilder pointBuilder;

    @Autowired
    private CostCalculator costCalculator;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE;
    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        PointPo curPoint = pointCache.getPoint();
        log.debug("curPoint = {}", curPoint);

        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);

        if (usage.getRecId() != null && usage.getRecId() > 0L) {
            checkPointRec(usage, pointCache);   // 校验失败会抛出异常
        }

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setPoint(curPoint.getPoint().subtract(usage.getPoint()))
                .setAvailable(curPoint.getAvailable().subtract(usage.getPoint()))
                .setUsed(curPoint.getUsed().add(usage.getPoint()));
        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setUsed(curPoint.getUsed()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.DECREASE, usage, curPoint);

        BigDecimal gap = usage.getPoint();
        BigDecimal cost = DecimalUtils.ZERO;
        List<PointRecLogDto> recLogs = new ArrayList<>();

        var recLogsRet = this.decreasePointLoop(usage.getType(),
                pointCache,
                pointLog, gap, usage.getRecId());
        gap = gap.subtract(recLogsRet.getDelta());
        cost = cost.add(recLogsRet.getDeltaCost());
        recLogs.addAll(recLogsRet.getRecLogs());
        log.debug("gap = {}", gap);

        if (DecimalUtils.gt(cost, DecimalUtils.ZERO)) {
            pointLog.setDeltaCost(cost).setCost(pointLog.getCost().subtract(cost));
            curPoint.setCost(curPoint.getCost().subtract(cost));
            point2Update.setCost(curPoint.getCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, recLogs);
        //return curPoint;
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost())
                .setRecLogList(recLogs)
                .setLogs(List.of(PointLogDto.build(pointLog)));
        return ret;
    }


    protected PointOpResult processPoint(PointUsage usage,
                                         PointCache pointCache,
                                         List<FreezeOrderRecPo> forList,
                                         PointOpResult unfreezeRet) {
        PointPo curPoint = pointCache.getPoint();
        log.debug("curPoint = {}", curPoint);

        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);

        if (usage.getRecId() != null && usage.getRecId() > 0L) {
            checkPointRec(usage, pointCache);   // 校验失败会抛出异常
        }

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setPoint(curPoint.getPoint().subtract(usage.getPoint()))
                .setAvailable(curPoint.getAvailable().subtract(usage.getPoint()))
                .setUsed(curPoint.getUsed().add(usage.getPoint()));
        var point2Update = unfreezeRet.getUpdateQ().getPoint();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setUsed(curPoint.getUsed()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.DECREASE, usage, curPoint);
        unfreezeRet.getUpdateQ().getLogs().add(pointLog);

        BigDecimal gap = usage.getPoint();
        BigDecimal cost = DecimalUtils.ZERO;
        //List<PointRecLogDto> recLogs = new ArrayList<>();

        var recLogsRet = this.decreasePointLoop(usage.getType(),
                pointCache,
                pointLog, gap, usage.getRecId());
        gap = gap.subtract(recLogsRet.getDelta());
        cost = cost.add(recLogsRet.getDeltaCost());
        unfreezeRet.getUpdateQ().getRecLogs().addAll(recLogsRet.getRecLogs());
        unfreezeRet.getUpdateQ().getRecList().addAll(recLogsRet.getRecList4Update());
        log.debug("gap = {}", gap);

        if (DecimalUtils.gt(cost, DecimalUtils.ZERO)) {
            pointLog.setDeltaCost(cost).setCost(pointLog.getCost().subtract(cost));
            curPoint.setCost(curPoint.getCost().subtract(cost));
            point2Update.setCost(curPoint.getCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), unfreezeRet.getUpdateQ().getRecList());
        if (CollectionUtils.isNotEmpty(forList)) {
            pointFlowService.closeFreezeOrderRec(usage.getType(), forList);
        }
        pointFlowService.addFlow(usage, unfreezeRet.getUpdateQ().getLogs(),
                unfreezeRet.getUpdateQ().getRecLogs());
        //return curPoint;
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost())
                .setRecLogList(unfreezeRet.getUpdateQ().getRecLogs())
                .setLogs(List.of(PointLogDto.build(pointLog)));
        return ret;
    }

    private LoopResult decreasePointLoop(String type, PointCache pointCache,
                                         PointLogPo pointLog, BigDecimal expected, Long recId) {
        log.info("decrease. type = {}, uid = {}, expected = {}, recId = {}",
                type, pointCache.getPoint().getUid(), expected, recId);

        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal deltaCost = DecimalUtils.ZERO;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            if (recId != null && recId > 0L && !recId.equals(rec.getId())) {
                continue;
            }
            BigDecimal gap = expected.subtract(sum);
            if (DecimalUtils.lte(gap, DecimalUtils.ZERO)) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (DecimalUtils.lte(rec.getAvailable(), DecimalUtils.ZERO)) {
                // do nothing
            } else if (DecimalUtils.lt(rec.getAvailable(), gap)) {
                sum = sum.add(rec.getAvailable());
                BigDecimal delta = rec.getAvailable();
                PointRecCalcResult calcResult = this.pointRecCalculator.decreasePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum = sum.add(gap);
                PointRecCalcResult calcResult = this.pointRecCalculator.decreasePoint(rec, gap);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost = deltaCost.add(calcResult.getDeltaCost());
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

    private boolean checkPointRec(PointUsage usage, PointCache pointCache) {
        var filter = pointCache.getPoint().getRecList().stream()
                .filter(rec -> rec.getId().equals(usage.getRecId())).findFirst();
        if (filter.isPresent()) {
            PointRecPo rec = filter.get();
            if (DecimalUtils.gte(rec.getAvailable(), usage.getPoint())) {
                return true;
            } else {
                log.warn("块内余额不足. rec = {}, usage = {}", rec, usage);
                throw new HyenaServiceException("块内余额不足");
            }
        } else {
            log.warn("积分块已消耗完. recId = {}, recIdList = {}",
                    usage.getRecId(),
                    pointCache.getPoint().getRecList().stream().map(PointRecPo::getId).collect(Collectors.toList()));
            throw new HyenaServiceException("积分块已消耗完");
        }

    }

}
