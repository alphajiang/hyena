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
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointUnfreezeStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.UNFREEZE;
    }

    @Override
    //@Transactional(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        log.info("unfreeze. usage = {}", usage);
        super.preProcess(usage);
        int retry = 3;
        PointPo curPoint = null;
        for(int i = 0; i < retry; i ++){
            try {
                curPoint = this.unfreeze(usage);
                if(curPoint != null) {
                    break;
                }
            } catch (Exception e) {
                log.warn("unfreeze failed. retry = {}, error = {}", retry, e.getMessage(), e);
            }
        }
        if(curPoint == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        pointFlowService.addFlow(getType(), usage, curPoint);
        return curPoint;

    }


    private PointPo unfreeze(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");


        curPoint.setAvailable(curPoint.getAvailable() + usage.getPoint())
                .setFrozen(curPoint.getFrozen() - usage.getPoint());
        var point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable()).setFrozen(curPoint.getFrozen())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());
        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if(!ret) {
            log.warn("unfreeze failed!!! please retry later. usage = {}", usage);
            return null;
        }
        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        //pointFlowService.addFlow(getType(), usage, curPoint);
        return curPoint;
    }
}
