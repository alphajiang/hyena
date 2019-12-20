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
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TestPointUnfreezeStrategy extends TestPointStrategyBase {

    private final String USAGE_TAG = "TAG_TestPointUnfreezeStrategy";

    @Autowired
    private PointStrategy pointFreezeStrategy;

    @Autowired
    private PointStrategy pointUnfreezeStrategy;

    /**
     * increase 100
     * freeze 80
     * unfreeze 50
     */
    @Test
    public void test_unfreezePoint() throws InterruptedException {
        log.info(">> test start");

        BigDecimal freezeNum = BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2);
        BigDecimal unfreezeNumber = BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectFrozen = BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2);    //freezeNum - unfreezeNumber
        BigDecimal expectAvailable = BigDecimal.valueOf(70L).setScale(DecimalUtils.SCALE_2); //this.point.getPoint() - freezeNum + unfreezeNumber;

        PointUsage freezeUsage = new PointUsage();
        freezeUsage.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum).setNote("test_unfreezePoint");
        this.point = this.pointFreezeStrategy.process(freezeUsage);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint(), result.getPoint());
        Assertions.assertEquals(expectAvailable, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(expectFrozen, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLogDto();
        expectPoingLog.setUid(super.getUid()).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2))
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(super.getUid()).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost((freezeNum.subtract(unfreezeNumber)).divide(BigDecimal.valueOf(2)))
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecIdList(List.of(pointRec.getId()))
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(3, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(expectFrozen).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(80L - 50L).setScale(DecimalUtils.SCALE_2).divide(BigDecimal.valueOf(2)))
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }

    /**
     * increase 100
     * freeze 20
     * freeze 30
     * unfreeze 40
     */
    @Test
    public void test_unfreezePoint_2times_freeze() throws InterruptedException {
        log.info(">> test start");

        BigDecimal[] freezeNum = {BigDecimal.valueOf(20L), BigDecimal.valueOf(30L)};
        BigDecimal unfreezeNumber = BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectFrozen = BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2);    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        BigDecimal expectAvailable = BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2); // 100 - 20 - 30 + 40

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[0]).setNote("test_unfreezePoint-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[1]).setNote("test_unfreezePoint-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint(), result.getPoint());
        Assertions.assertEquals(expectAvailable, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(expectFrozen, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());

        Thread.sleep(300L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLogDto();
        expectPoingLog.setUid(super.getUid()).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(5L).setScale(DecimalUtils.SCALE_2))
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(super.getUid()).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(20L + 30L - 40L).setScale(DecimalUtils.SCALE_2).divide(BigDecimal.valueOf(2)))
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecIdList(List.of(pointRec.getId()))
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(4, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2, freeze, 3: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(expectFrozen).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(BigDecimal.valueOf(20L + 30L - 40L).divide(BigDecimal.valueOf(2))
                        .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP))
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }


    /**
     * increase 100
     * freeze 20
     * freeze 30
     * unfreeze 50
     */
    @Test
    public void test_unfreezePoint_using_orderNo() throws InterruptedException {
        log.info(">> test start");

        BigDecimal[] freezeNum = {BigDecimal.valueOf(20L), BigDecimal.valueOf(30L)};
        BigDecimal unfreezeNumber = BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectFrozen = DecimalUtils.ZERO;    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        BigDecimal expectAvailable = BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2); // 100 - 20 - 30 + 50
        String orderNo = UUID.randomUUID().toString();

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[0])
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[1])
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);

        Thread.sleep(100L);
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG)
                .setUnfreezeByOrderNo(true)
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint(), result.getPoint());
        Assertions.assertEquals(expectAvailable, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(expectFrozen, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());

        Thread.sleep(300L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(super.getUid()).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPointLog = new PointLogDto();
        expectPointLog.setUid(super.getUid()).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(DecimalUtils.ZERO)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
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
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecIdList(List.of(pointRec.getId()))
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(4, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2, freeze, 3: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(expectFrozen).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }
}
