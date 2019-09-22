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
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PointDecreaseFrozenStrategy extends AbstractPointStrategy {

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

    @Override
    public CalcType getType() {
        return CalcType.DECREASE_FROZEN;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        log.info("decrease frozen. usage = {}", usage);
        super.preProcess(usage);
        int retry = 3;
        DecreaseResult ret = null;
        for(int i = 0; i < retry; i ++){
            try {
                ret = this.decrease(usage);
                if(ret != null) {
                    break;
                }
            }
            catch (HyenaParameterException e) {
                throw e;
            }
            catch (Exception e) {
                log.warn("decrease frozen failed. retry = {}, error = {}", retry, e.getMessage(), e);
            }
        }
        if(ret == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        if(ret.getPostUnfreeze() != null) {
            pointFlowService.addFlow(CalcType.UNFREEZE, ret.getUsage4Unfreeze(), ret.getPostUnfreeze());
        }
        pointFlowService.addFlow(CalcType.DECREASE, usage, ret.getPostDecrease());
        return ret.getPostDecrease();
    }



    private DecreaseResult decrease(PointUsage usage) {
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
        PointPo postUnfreeze = null;
        PointUsage usage4Unfreeze = null;
        if(usage.getUnfreezePoint() != null && usage.getUnfreezePoint() > 0L) {
            // 解冻
            curPoint.setFrozen(curPoint.getFrozen() - usage.getUnfreezePoint())
                    .setAvailable(curPoint.getAvailable() + usage.getUnfreezePoint());
            postUnfreeze = new PointPo();
            BeanUtils.copyProperties(curPoint, postUnfreeze);
            postUnfreeze.setSeqNum(postUnfreeze.getSeqNum() + 1);
            usage4Unfreeze = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Unfreeze);
            usage4Unfreeze.setPoint(usage.getUnfreezePoint());

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

        DecreaseResult result = new DecreaseResult(postUnfreeze, usage4Unfreeze, curPoint);
        //pointFlowService.addFlow(CalcType.DECREASE, usage, curPoint);
        return result;
    }

    @Data
    @AllArgsConstructor
    class DecreaseResult {
        PointPo postUnfreeze;
        PointUsage usage4Unfreeze;
        PointPo postDecrease;
    }

}
