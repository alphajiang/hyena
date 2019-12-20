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

package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class PointBuilder {


    public PointLogPo buildPointLog(@NonNull PointOpType actionType,
                                    @NonNull PointUsage usage, @NonNull PointPo point) {
        PointLogPo pointLog = new PointLogPo();
        pointLog.setPid(point.getId())
                .setUid(point.getUid())
                .setSeqNum(point.getSeqNum())
                .setDelta(usage.getPoint())
                .setPoint(point.getPoint())
                .setAvailable(point.getAvailable())
                .setUsed(point.getUsed())
                .setFrozen(point.getFrozen())
                .setRefund(point.getRefund())
                .setExpire(point.getExpire())
                .setCost(point.getCost())
                .setFrozenCost(point.getFrozenCost())
                .setType(actionType.code())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(usage.getSourceType())
                .setOrderType(usage.getOrderType())
                .setPayType(usage.getPayType())
                .setExtra(usage.getExtra());
        if (usage.getCost() != null) {
            pointLog.setDeltaCost(usage.getCost());
        } else {
            pointLog.setDeltaCost(DecimalUtils.ZERO);
        }
        if (pointLog.getOrderNo() == null) {
            pointLog.setOrderNo("");
        }
        if (StringUtils.isNotBlank(usage.getTag())) {
            pointLog.setTag(usage.getTag());
        } else {
            pointLog.setTag("");
        }
        if (StringUtils.isNotBlank(usage.getNote())) {
            pointLog.setNote(usage.getNote());
        } else {
            pointLog.setNote("");
        }
        return pointLog;
    }


    public PointRecLogDto buildRecLog(PointRecPo rec, PointLogPo pointLog,
                                      BigDecimal delta, BigDecimal deltaCost) {
        PointRecLogDto recLog = new PointRecLogDto();
        recLog.setRecOrigOrderNo(rec.getOrderNo())
                .setPid(rec.getPid()).setSeqNum(pointLog.getSeqNum())
                .setRecId(rec.getId()).setType(pointLog.getType())
                .setDelta(delta).setDeltaCost(deltaCost);
        recLog.setAvailable(rec.getAvailable() == null ? DecimalUtils.ZERO : rec.getAvailable());
        recLog.setUsed(rec.getUsed() == null ? DecimalUtils.ZERO : rec.getUsed());
        recLog.setFrozen(rec.getFrozen() == null ? DecimalUtils.ZERO : rec.getFrozen());
        recLog.setRefund(rec.getRefund() == null ? DecimalUtils.ZERO : rec.getRefund());
        recLog.setCancelled(rec.getCancelled() == null ? DecimalUtils.ZERO : rec.getCancelled());
        recLog.setExpire(rec.getExpire() == null ? DecimalUtils.ZERO : rec.getExpire());
        recLog.setCost(rec.getTotalCost() .subtract(rec.getUsedCost()));
        recLog.setFrozenCost(rec.getFrozenCost());
        recLog.setUsedCost(rec.getUsedCost());
        recLog.setRefundCost(rec.getRefundCost());
        recLog.setSourceType(pointLog.getSourceType()).setOrderType(pointLog.getOrderType())
                .setPayType(pointLog.getPayType());
        recLog.setNote(pointLog.getNote() == null ? "" : pointLog.getNote());
        return recLog;
    }

    public FreezeOrderRecPo buildFreezeOrderRec(@NonNull PointPo point, @NonNull PointRecPo rec,
                                                Integer orderType,
                                                String orderNo, BigDecimal frozen, BigDecimal frozenCost) {
        FreezeOrderRecPo fo = new FreezeOrderRecPo();
        fo.setPid(point.getId())
                .setUid(point.getUid())
                .setRecId(rec.getId())
                .setSeqNum(point.getSeqNum())
                .setOrderType(orderType == null ? 0 : orderType)
                .setOrderNo(orderNo)
                .setFrozen(frozen)
                .setFrozenCost(frozenCost)
                .setEnable(true)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
        return fo;
    }
}
