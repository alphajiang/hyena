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
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaTestAssert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

        long freezeNum = 80L;
        long unfreezeNumber = 50L;
        long expectFrozen = 30L;    //freezeNum - unfreezeNumber
        long expectAvailable = 70L; //this.point.getPoint() - freezeNum + unfreezeNumber;

        PointUsage freezeUsage = new PointUsage();
        freezeUsage.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum).setNote("test_unfreezePoint");
        this.point = this.pointFreezeStrategy.process(freezeUsage);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assertions.assertEquals(expectAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(expectFrozen, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber/2)
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen)
                .setExpire(0L)
                .setRefund(0L)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(15L)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost((freezeNum - unfreezeNumber) / 2)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecId(pointRec.getId())
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(3, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(0L)
                .setFrozen(expectFrozen).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost((80L - 50L) / 2)
                .setUsedCost(0L)
                .setRefundCost(0L)
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

        long[] freezeNum = {20L, 30L};
        long unfreezeNumber = 40L;
        long expectFrozen = 10L;    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        long expectAvailable = 90L; // 100 - 20 - 30 + 40

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[0]).setNote("test_unfreezePoint-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[1]).setNote("test_unfreezePoint-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assertions.assertEquals(expectAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(expectFrozen, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(300L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber/2)
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen)
                .setExpire(0L)
                .setRefund(0L)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(5L)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost((20L + 30L - 40L)/2)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecId(pointRec.getId())
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(4, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2, freeze, 3: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(0L)
                .setFrozen(expectFrozen).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1 )
                .setFrozenCost((20L + 30L - 40L) / 2)
                .setUsedCost(0L)
                .setRefundCost(0L)
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

        long[] freezeNum = {20L, 30L};
        long unfreezeNumber = 50L;
        long expectFrozen = 0L;    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        long expectAvailable = 100L; // 100 - 20 - 30 + 50
        String orderNo = UUID.randomUUID().toString();

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[0])
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[1])
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);

        Thread.sleep(100L);
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG)
                .setUnfreezeByOrderNo(true)
                .setOrderNo(orderNo)
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assertions.assertEquals(expectAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(expectFrozen, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(300L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPointLog = new PointLog();
        expectPointLog.setUid(this.uid).setType(PointOpType.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber/2)
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen)
                .setExpire(0L)
                .setRefund(0L)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(0L)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(0L)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec);

        // verify point record logs
        var listPointRecLogParam = new ListPointRecLogParam();
        SortParam pointRecLogSortParam = new SortParam();
        pointRecLogSortParam.setColumns(List.of("log.id")).setOrder(SortOrder.desc);
        listPointRecLogParam.setRecId(pointRec.getId())
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(4, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2, freeze, 3: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(0L)
                .setFrozen(expectFrozen).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1 )
                .setFrozenCost(0L)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }
}
