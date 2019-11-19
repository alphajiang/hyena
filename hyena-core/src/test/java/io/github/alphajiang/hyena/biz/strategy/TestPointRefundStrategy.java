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
import io.github.alphajiang.hyena.utils.HyenaTestAssert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

        long refundNum = 40L;

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setCost(refundNum)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_refundPoint");
        PointPo result = this.strategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue() - refundNum * 2, result.getPoint().longValue());
        Assertions.assertEquals(20L, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(0L, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());
        Assertions.assertEquals(80L, result.getRefund().longValue());
        Assertions.assertEquals(10L, result.getCost().longValue());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLogDto();
        expectPoingLog.setUid(this.uid).setType(PointOpType.REFUND.code())
                .setSeqNum(result.getSeqNum()).setDelta(refundNum * 2)
                .setDeltaCost(refundNum)
                .setDeltaCost(refundNum)
                .setPoint(INCREASE_POINT_1 - refundNum * 2)
                .setAvailable(20L)
                .setUsed(0L).setFrozen(0L)
                .setExpire(0L)
                .setRefund(80L)
                .setCost(INCREASE_COST_1 - refundNum)
                .setFrozenCost(0L)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(20L)
                .setUsed(0L).setFrozen(0L).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(0L)
                .setUsedCost(0L)
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
                .setType(PointOpType.REFUND.code()).setDelta(refundNum * 2)
                .setAvailable(20L)
                .setUsed(0L)
                .setFrozen(0L).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(0L)
                .setUsedCost(0L)
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

        long[] freezeNum = {10L, 30L};
        long unfreezeNumber = 40L;
        long expectFrozen = 0L;    //freezeNum[0] + freezeNum[1] - unfreezeNumber
        long expectAvailable = 60L; // 100 - 40
        String orderNo = UUID.randomUUID().toString();

        PointUsage freezeUsage1 = new PointUsage();
        freezeUsage1.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[0])
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo-1");
        this.point = this.pointFreezeStrategy.process(freezeUsage1);

        PointUsage freezeUsage2 = new PointUsage();
        freezeUsage2.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum[1])
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo-2");
        this.point = this.pointFreezeStrategy.process(freezeUsage2);
        Thread.sleep(100L);

        log.info("======>");
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setCost(unfreezeNumber / 2)
                .setUnfreezePoint(unfreezeNumber)
                .setTag(USAGE_TAG)
                .setUnfreezeByOrderNo(true)
                .setOrderNo(orderNo)
                .setNote("test_refundPoint_using_orderNo");
        PointPo result = this.strategy.process(usage);
        log.info("<======");

        log.info("result = {}", result);
        Assertions.assertEquals(super.INCREASE_POINT_1 - unfreezeNumber, result.getPoint().longValue());
        Assertions.assertEquals(expectAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(expectFrozen, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());
        Assertions.assertEquals(30L, result.getCost().longValue());

        Thread.sleep(300L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPointLog = new PointLogDto();
        expectPointLog.setUid(this.uid).setType(PointOpType.REFUND.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setDeltaCost(unfreezeNumber / 2)
                .setPoint(INCREASE_POINT_1 - unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen)
                .setExpire(0L)
                .setRefund(unfreezeNumber)
                .setCost(INCREASE_COST_1 - unfreezeNumber / 2)
                .setFrozenCost(0L)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(listPointRecParam);
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
                .setRefundCost(unfreezeNumber / 2)
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
                .setUsed(0L)
                .setFrozen(expectFrozen).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(0L)
                .setUsedCost(0L)
                .setRefundCost(unfreezeNumber/2)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }
}
