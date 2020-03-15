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

import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PointExpireStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointExpireStrategy.class);

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
    private PointBuilder pointBuilder;

    @Override
    public CalcType getType() {
        return CalcType.EXPIRE;
    }

    private Boolean doUpdate = false;

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        logger.info("expire. usage = {}", usage);
        //super.preProcess(usage, false);
        //HyenaAssert.notNull(usage.getRecId(), "invalid parameter, 'recId' can't be null");
        //HyenaAssert.isTrue(usage.getRecId().longValue() > 0L, "invalid parameter: recId");

        PointPo curPoint = pointCache.getPoint();


        List<PointRecPo> recList4Update = new ArrayList<>();

        synchronized (doUpdate) {
            doUpdate = false;
            pointCache.getPoint().getRecList().stream()
                    .filter(rec -> DecimalUtils.gt(rec.getAvailable(), DecimalUtils.ZERO))
                    .filter(rec -> rec.getExpireTime() != null && rec.getExpireTime().before(Calendar.getInstance().getTime()))
                    .forEach(rec -> {
                        usage.setPoint(rec.getAvailable());
                        BigDecimal delta = rec.getAvailable();
                        BigDecimal deltaCost = rec.getTotalCost().subtract(rec.getUsedCost()).subtract(rec.getFrozenCost());
                        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                                .setAvailable(curPoint.getAvailable().subtract(rec.getAvailable()))
                                .setPoint(curPoint.getPoint().subtract(rec.getAvailable()))
                                .setExpire(curPoint.getExpire().add(rec.getAvailable()))
                                .setCost(curPoint.getCost().subtract(deltaCost));
                        recList4Update.add(pointRecDs.expirePointRec(rec));
                        if (DecimalUtils.gt(deltaCost, BigDecimal.ZERO)) {
                            usage.setCost(deltaCost);
                        }
                        PointLogPo pl = this.pointBuilder.buildPointLog(PointOpType.EXPIRE, usage, curPoint);
                        List<PointRecLogDto> recLogList = new ArrayList<>();
                        recLogList.add(pointBuilder.buildRecLog(rec, pl, delta, deltaCost));

                        pointFlowService.addFlow(usage, pl, recLogList);
                        doUpdate = true;
                    });
            if(Boolean.TRUE.equals(doUpdate)) {
                List<PointRecPo> recList = pointCache.getPoint().getRecList().stream().filter(rec -> rec.getEnable())
                        .collect(Collectors.toList());
                pointCache.getPoint().setRecList(recList);

                pointFlowService.updatePoint(usage.getType(), curPoint);
                pointFlowService.updatePointRec(usage.getType(), recList4Update);
            }
        }
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        return ret;
    }
}