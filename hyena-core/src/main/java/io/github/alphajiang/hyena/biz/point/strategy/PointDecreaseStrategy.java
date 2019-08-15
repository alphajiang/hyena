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
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PointDecreaseStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointDecreaseStrategy.class);

    @Autowired
    private PointDs pointDs;


    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE;
    }

    @Override
    //@Transactional
    public PointPo process(PointUsage usage) {
        logger.info("decrease. usage = {}", usage);
        super.preProcess(usage);
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        logger.debug("curPoint = {}", curPoint);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
//        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= usage.getPoint(),
//                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
//                "no enough available point. current available point is " + curPoint.getAvailable(),
//                Level.WARN,
//                HyenaNoPointException.class);



        curPoint.setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setUsed(curPoint.getUsed() + usage.getPoint());
//        if(curPoint.getFrozen() < 0L) {
//            // 使用可用余额来抵扣超扣部分
//            curPoint.setAvailable(curPoint.getAvailable() + curPoint.getFrozen());
//            curPoint.setFrozen(0L);
//        }
        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setUsed(curPoint.getUsed()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());
        this.pointDs.update(usage.getType(), point2Update);
        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        pointFlowService.addFlow(getType(), usage, curPoint);
        return curPoint;
    }


}
