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
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.ds.service.PointService;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PointCancelStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointCancelStrategy.class);

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRecService pointRecService;

    @Override
    public CalcType getType() {
        return CalcType.CANCEL;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        logger.info("cancel. usage = {}", usage);
        super.preProcess(usage);
        HyenaAssert.notNull(usage.getRecId(), "invalid parameter, 'recId' can't be null");
        HyenaAssert.isTrue(usage.getRecId().longValue() > 0L, "invalid parameter: recId");

        PointPo curPoint = this.pointService.getCusPoint(usage.getType(), usage.getCusId(), true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the cusId: " + usage.getCusId(), Level.WARN);
        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point");

        PointRecPo rec = this.pointRecService.getById(usage.getType(), usage.getRecId(), true);
        HyenaAssert.notNull(rec, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point record with id = " + usage.getRecId(), Level.WARN);
        if (rec.getCancelled() > 0) {
            return curPoint;
        }
        logger.info("curPoint = {}", curPoint);
        logger.info("rec = {}", rec);
        HyenaAssert.isTrue(rec.getFrozen().longValue() < 1L,
                HyenaConstants.RES_CODE_STATUS_ERROR,
                "can't cancel frozen point record");
        HyenaAssert.isTrue(rec.getPid() == curPoint.getId(), "invalid parameter.");
        HyenaAssert.isTrue(rec.getAvailable().longValue() == usage.getPoint(), "point mis-match");
        long delta = rec.getAvailable();
        this.pointRecService.cancelPointRec(usage.getType(), rec, usage.getNote());


        curPoint.setAvailable(curPoint.getAvailable() - delta)
                .setPoint(curPoint.getPoint() - delta);
        var point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable())
                .setPoint(curPoint.getPoint()).setId(curPoint.getId());
        this.pointService.update(usage.getType(), point2Update);
        return curPoint;
    }
}
