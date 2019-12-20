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
import io.github.alphajiang.hyena.biz.point.strategy.PointRefundStrategy;
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
import java.util.UUID;

@Slf4j
public class TestPointRefundStrategy extends TestPointStrategyBase {

    private final String USAGE_TAG = "TAG_TestPointRefundStrategy";

    @Autowired
    private PointRefundStrategy strategy;

    @Autowired
    private PointStrategy pointFreezeStrategy;

    /**
     * increase 100
     * refund 80
     */
    @Test
    public void test_refundPoint() throws InterruptedException {
        log.info(">> test start");

        BigDecimal refundNum = BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setCost(refundNum)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_refundPoint");
        PointPo result = this.strategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().subtract(refundNum.multiply(BigDecimal.valueOf(2))), result.getPoint());
        Assertions.assertEquals(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2), result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());
        Assertions.assertEquals(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2), result.getRefund());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getCost());

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
        expectPoingLog.setUid(super.getUid()).setType(PointOpType.REFUND.code())
                .setSeqNum(result.getSeqNum()).setDelta(refundNum.multiply(BigDecimal.valueOf(2)))
                .setDeltaCost(refundNum)
                .setDeltaCost(refundNum)
                .setPoint(INCREASE_POINT_1.subtract(refundNum.multiply(BigDecimal.valueOf(2))))
                .setAvailable(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setUsed(DecimalUtils.ZERO).setFrozen(DecimalUtils.ZERO)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setCost(INCREASE_COST_1.subtract(refundNum))
                .setFrozenCost(DecimalUtils.ZERO)
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
                .setTotal(INCREASE_POINT_1)
                .setAvailable(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setUsed(DecimalUtils.ZERO).setFrozen(DecimalUtils.ZERO)
                .setExpire(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(refundNum)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setSeqNumList(List.of(result.getSeqNum()))
                .setRecIdList(List.of(pointRec.getId()))
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(1, pointRecLogList.size());
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.REFUND.code()).setDelta(refundNum.multiply(BigDecimal.valueOf(2)))
                .setAvailable(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(DecimalUtils.ZERO).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(refundNum)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }


    /**
     * increase 100
     * freeze 10
     * freeze 30
     * unfreeze+refund 40
     */
    @Test
    public void test_refundPoint_using_orderNo() throws InterruptedException {
        log.info(">> test start");

        BigDecimal[] freezeNum = {
                BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2),
                BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2)
        };
        BigDecimal unfreezeNumber = BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2);
        BigDecimal expectFrozen = DecimalUtils.ZERO;    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        BigDecimal expectAvailable = BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2); // 100 - 40
        String orderNo = UUID.randomUUID().toString();

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[0])
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(super.getUid()).setPoint(freezeNum[1])
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);
        Thread.sleep(200L);

        log.info("======>");
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(unfreezeNumber)
                .setCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setUnfreezePoint(unfreezeNumber)
                .setTag(USAGE_TAG)
                .setUnfreezeByOrderNo(true)
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo");
        PointPo result = this.strategy.process(usage);
        log.info("<======");

        log.info("result = {}", result);
        Assertions.assertEquals(super.INCREASE_POINT_1.subtract(unfreezeNumber), result.getPoint());
        Assertions.assertEquals(expectAvailable, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(expectFrozen, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), result.getCost());

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
        expectPointLog.setUid(super.getUid()).setType(PointOpType.REFUND.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setPoint(INCREASE_POINT_1.subtract(unfreezeNumber))
                .setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO).setFrozen(expectFrozen)
                .setExpire(DecimalUtils.ZERO)
                .setRefund(unfreezeNumber)
                .setCost(INCREASE_COST_1.subtract(unfreezeNumber.divide(BigDecimal.valueOf(2))))
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
                .setRefundCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
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
        Assertions.assertEquals(5, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2, freeze, 3: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.REFUND.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(DecimalUtils.ZERO)
                .setFrozen(expectFrozen).setCancelled(DecimalUtils.ZERO).setExpire(DecimalUtils.ZERO)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setRefundCost(unfreezeNumber.divide(BigDecimal.valueOf(2)))
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }
}
