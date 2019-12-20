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
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
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

@Slf4j
public class TestPointDecreaseFrozenStrategy extends TestPointStrategyBase {


    @Autowired
    private PointStrategy pointDecreaseFrozenStrategy;

    @Autowired
    private PointStrategy pointFreezeStrategy;

    @Autowired
    private PointStrategy pointIncreaseStrategy;


    /**
     * increase 100
     * freeze 80
     * decreaseFrozen 70 (unfreeze80)
     */
    @Test
    public void test_decreasePointUnfreeze() throws InterruptedException {
        log.info(">> test start");

        log.info("point = {}", this.point);
        BigDecimal freezeNum = BigDecimal.valueOf(80).setScale(DecimalUtils.SCALE_2);
        BigDecimal useNumber = BigDecimal.valueOf(70L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectPoint = BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectAvailable = BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectFrozen = DecimalUtils.ZERO;


        // 先冻结
        PointUsage freezeUsage = new PointUsage();
        freezeUsage.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum)
                .setNote("test_decreasePointUnfreeze");
        PointPo retPoint = this.pointFreezeStrategy.process(freezeUsage);
        log.info("point = {}", retPoint);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(useNumber)
                .setUnfreezePoint(freezeNum)
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setNote("test_decreasePointUnfreeze");
        PointPo result = this.pointDecreaseFrozenStrategy.process(usage);
        log.info("result = {}", result);
        Thread.sleep(200L);
        Assertions.assertEquals(expectPoint, result.getPoint());
        Assertions.assertEquals(expectAvailable, result.getAvailable());
        Assertions.assertEquals(useNumber, result.getUsed());
        Assertions.assertEquals(expectFrozen, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());


        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum() - 1)
                .setSourceTypes(List.of(DECREASE_SOURCE_TYPE))
                .setOrderTypes(List.of(DECREASE_ORDER_TYPE))
                .setPayTypes(List.of(DECREASE_PAY_TYPE))
                .setType(super.getPointType())
                .setSorts(List.of(SortParam.as("log.id", SortOrder.asc)));
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLogUnfreeze = pointLogs.get(0);
        var expectPoingLogUnfreeze = new PointLogDto();
        expectPoingLogUnfreeze.setUid(super.getUid()).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum() - 1).setDelta(freezeNum)
                .setDeltaCost(freezeNum.divide(BigDecimal.valueOf(2)))
                .setPoint(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1)
                .setUsed(DecimalUtils.ZERO).setFrozen(DecimalUtils.ZERO)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLogUnfreeze, pointLogUnfreeze);

        listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum())
                .setSourceTypes(List.of(DECREASE_SOURCE_TYPE))
                .setOrderTypes(List.of(DECREASE_ORDER_TYPE))
                .setPayTypes(List.of(DECREASE_PAY_TYPE))
                .setType(super.getPointType())
                .setSorts(List.of(SortParam.as("log.id", SortOrder.asc)));
        pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        var pointLogDecrease = pointLogs.get(0);
        var expectPoingLogDecrease = new PointLogDto();
        expectPoingLogDecrease.setUid(super.getUid()).setType(PointOpType.DECREASE.code())
                .setSeqNum(result.getSeqNum()).setDelta(useNumber)
                .setDeltaCost(useNumber.divide(BigDecimal.valueOf(2)))
                .setPoint(result.getPoint()).setAvailable(result.getAvailable())
                .setUsed(result.getUsed()).setFrozen(result.getFrozen())
                .setExpire(result.getExpire())
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1.subtract(useNumber.divide(BigDecimal.valueOf(2))))
                .setFrozenCost(DecimalUtils.ZERO)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLogDecrease, pointLogDecrease);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(super.getUid()).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1.subtract(useNumber))
                .setUsed(useNumber).setFrozen(DecimalUtils.ZERO)
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(useNumber.divide(BigDecimal.valueOf(2)))
                .setRefundCost(DecimalUtils.ZERO)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1)
                .setSourceType(INCREASE_SOURCE_TYPE)
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
        Assertions.assertEquals(4, pointRecLogList.size()); // [decrease, unfreeze, freeze, increase]
        var pointRecLogDecrease = pointRecLogList.get(0);
        var expectPointRecLogDecrease = new PointRecLogPo();
        expectPointRecLogDecrease.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.DECREASE.code()).setDelta(useNumber)
                .setAvailable(expectAvailable)
                .setUsed(useNumber)
                .setFrozen(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1.subtract(useNumber.divide(BigDecimal.valueOf(2))))
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(useNumber.divide(BigDecimal.valueOf(2)))
                .setRefundCost(DecimalUtils.ZERO)
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLogDecrease, pointRecLogDecrease);


        var pointRecLogUnfreeze = pointRecLogList.get(1);
        var expectPointRecLogUnfreeze = new PointRecLogPo();
        expectPointRecLogUnfreeze.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum() - 1).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(freezeNum)
                .setAvailable(INCREASE_POINT_1)
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLogUnfreeze, pointRecLogUnfreeze);


        log.info("<< test end");
    }
}
