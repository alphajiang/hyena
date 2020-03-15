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
import io.github.alphajiang.hyena.model.vo.PointVo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.NumberUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PointFreezeByRecIdStrategy extends AbstractPointStrategy {

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
        return CalcType.FREEZE_BY_REC_ID;
    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        if (StringUtils.isBlank(usage.getOrderNo())) {
            log.warn("订单号不能为空. usage = {}", usage);
            throw new HyenaParameterException("订单号不能为空");
        }
        PointVo curPoint = pointCache.getPoint();
        if (DecimalUtils.lt(curPoint.getAvailable(), usage.getPoint())) {
            log.warn("no enough available point. usage = {}, curPoint = {}", usage, curPoint);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        }

        //PointRecPo rec = null;
        Optional<PointRecPo> recOpt = curPoint.getRecList()
                .stream().filter(rec -> NumberUtils.longEquals(rec.getId(), usage.getRecId())).findFirst();
        if (recOpt.isEmpty()) {
            log.warn("point rec is not exists. recId = {}", usage.getRecId());
            throw new HyenaParameterException("point rec is not exists!");
        } else if (recOpt.get().getAvailable().compareTo(usage.getPoint()) != 0) {
            log.warn("point mis-match. rec = {}, usage = {}", recOpt.get(), usage);
            throw new HyenaParameterException("point mis-match!");
        }
        PointRecPo rec = recOpt.get();
        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setAvailable(curPoint.getAvailable().subtract(usage.getPoint()))
                .setFrozen(curPoint.getFrozen().add(usage.getPoint()));
        PointPo point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable())
                .setFrozen(curPoint.getFrozen()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.FREEZE, usage, curPoint);
        BigDecimal gap = usage.getPoint();
        List<PointRecLogDto> recLogs = new ArrayList<>();

        PointRecCalcResult result = pointRecCalculator.freezePoint(rec, usage.getPoint());
        List<PointRecPo> recList4Update = List.of(result.getRec4Update());

        FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                rec, usage.getOrderType(), usage.getOrderNo(), gap, result.getDeltaCost());
        List<FreezeOrderRecPo> forList = List.of(fo);

        var recLog = this.pointBuilder.buildRecLog(rec, pointLog, gap, result.getDeltaCost());
        recLogs.add(recLog);

        BigDecimal cost = result.getDeltaCost();

        if (DecimalUtils.gt(cost, DecimalUtils.ZERO)) {
            pointLog.setDeltaCost(cost).setFrozenCost(pointLog.getFrozenCost().add(cost));
            curPoint.setFrozenCost(curPoint.getFrozenCost().add(cost));
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);

        pointFlowService.updatePointRec(usage.getType(), recList4Update);
        pointFlowService.addFreezeOrderRec(usage.getType(), forList);
        pointFlowService.addFlow(usage, pointLog, recLogs);

        pointCache.setUpdateTime(new Date());
        //return curPoint;
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(gap)
                .setOpCost(cost);
        return ret;
    }


}
