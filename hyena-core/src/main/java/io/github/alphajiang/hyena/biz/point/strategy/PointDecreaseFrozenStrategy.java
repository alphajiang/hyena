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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointDecreaseFrozenStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;


    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE_FROZEN;
    }

    @Override
    //@Transactional(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        log.info("decrease frozen. usage = {}", usage);
        super.preProcess(usage);
        int retry = 3;
        PointPo curPoint = null;
        for(int i = 0; i < retry; i ++){
            try {
                curPoint = this.decrease(usage);
                if(curPoint != null) {
                    break;
                }
            } catch (Exception e) {
                log.warn("decrease frozen failed. retry = {}, error = {}", retry, e.getMessage(), e);
            }
        }
        if(curPoint == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        pointFlowService.addFlow(getType(), usage, curPoint);
        return curPoint;
//        HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");
//       // var cusPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
//        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
//        pointFlowService.addFlow(CalcType.DECREASE, usage, curPoint);
//
//
//        return curPoint;
    }


    private PointPo decrease(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getFrozen(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        if(usage.getUnfreezePoint() != null) {
            HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getUnfreezePoint(),
                    HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                    "no enough frozen point");
        }

        if(usage.getUnfreezePoint() != null && usage.getUnfreezePoint() > 0L) {
            // 解冻
            curPoint.setFrozen(curPoint.getFrozen() - usage.getUnfreezePoint())
                    .setAvailable(curPoint.getAvailable() + usage.getUnfreezePoint());
            PointPo postUnfreeze = new PointPo();
            BeanUtils.copyProperties(curPoint, postUnfreeze);
            postUnfreeze.setSeqNum(postUnfreeze.getSeqNum() + 1);
            PointUsage usage4Unfreeze = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Unfreeze);
            usage4Unfreeze.setPoint(usage.getUnfreezePoint());
            pointFlowService.addFlow(CalcType.UNFREEZE, usage4Unfreeze, postUnfreeze);
        }
        curPoint.setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setUsed(curPoint.getUsed() + usage.getPoint());
        if(curPoint.getFrozen() < 0L) {
            // 使用可用余额来抵扣超扣部分
            curPoint.setAvailable(curPoint.getAvailable() + curPoint.getFrozen());
            curPoint.setFrozen(0L);
        }

        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint()).setFrozen(curPoint.getFrozen())
                .setUsed(curPoint.getUsed()).setAvailable(curPoint.getAvailable())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if(!ret) {
            log.warn("decrease frozen failed!!! please retry later. usage = {}", usage);
            return null;
        }
        HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");
        // var cusPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        //pointFlowService.addFlow(CalcType.DECREASE, usage, curPoint);
        return curPoint;
    }

}
