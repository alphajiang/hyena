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

package io.github.alphajiang.hyena.biz.strategy;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.strategy.PointFreezeCostStrategy;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaTestAssert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TestPointFreezeCostStrategy extends TestPointStrategyBase {
    private final String USAGE_TAG = "TAG_TestPointFreezeCostStrategy";

    @Autowired
    private PointFreezeCostStrategy strategy;


    @Test
    public void test_freezeCost() throws InterruptedException {
        log.info(">> test start");
        BigDecimal number = BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2);
        BigDecimal resultAvailable = BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2); // 100 - 20*2
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setCost(number)
                .setOrderNo(UUID.randomUUID().toString()).setTag(USAGE_TAG)
                .setSourceType(FREEZE_SOURCE_TYPE).setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote("test_freezeCost");
        PointPo result = this.strategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint(), result.getPoint());
        Assertions.assertEquals(resultAvailable, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2), result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());
        Assertions.assertEquals(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2), result.getFrozenCost());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum())
                .setSourceTypes(List.of(FREEZE_SOURCE_TYPE))
                .setOrderTypes(List.of(FREEZE_ORDER_TYPE))
                .setPayTypes(List.of(FREEZE_PAY_TYPE))
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPointLog = new PointLogDto();
        expectPointLog.setUid(super.getUid()).setType(PointOpType.FREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(number.multiply(BigDecimal.valueOf(2)))
                .setDeltaCost(number)
                .setPoint(super.INCREASE_POINT_1)
                .setAvailable(INCREASE_POINT_1.subtract(number.multiply(BigDecimal.valueOf(2))))
                .setUsed(DecimalUtils.ZERO).setFrozen(number.multiply(BigDecimal.valueOf(2)))
                .setExpire(DecimalUtils.ZERO)
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(number)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(super.getUid()).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1.subtract(number.multiply(BigDecimal.valueOf(2))))
                .setUsed(DecimalUtils.ZERO).setFrozen(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1)
                .setSourceType(super.INCREASE_SOURCE_TYPE)
                .setOrderType(INCREASE_ORDER_TYPE)
                .setPayType(INCREASE_PAY_TYPE);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecIdList(List.of(pointRec.getId()))
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(2, pointRecLogList.size()); // 0: freeze; 1: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.FREEZE.code()).setDelta(number.multiply(BigDecimal.valueOf(2)))
                .setAvailable(INCREASE_POINT_1.subtract(number.multiply(BigDecimal.valueOf(2))))
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(number.multiply(BigDecimal.valueOf(2)).setScale(DecimalUtils.SCALE_2))
                .setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(number)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);

        log.info("<< test end");
    }
}
