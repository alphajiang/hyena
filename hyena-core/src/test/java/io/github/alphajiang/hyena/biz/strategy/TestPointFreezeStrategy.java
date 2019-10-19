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
public class TestPointFreezeStrategy extends TestPointStrategyBase {

    private final String USAGE_TAG = "TAG_TestPointFreezeStrategy";

    @Autowired
    private PointStrategy pointFreezeStrategy;


    @Autowired
    private PointStrategy pointIncreaseStrategy;

    /**
     * 1, increase 100
     * 2, freeze 80
     */
    @Test
    public void test_freezePoint() throws InterruptedException {
        log.info(">> test start");
        long number = 80L;
        long resultAvailable = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number)
                .setOrderNo(UUID.randomUUID().toString()).setTag(USAGE_TAG)
                .setSourceType(FREEZE_SOURCE_TYPE).setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote("test_freezePoint");
        PointPo result = this.pointFreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assertions.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(number, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setSourceTypes(List.of(FREEZE_SOURCE_TYPE))
                .setOrderTypes(List.of(FREEZE_ORDER_TYPE))
                .setPayTypes(List.of(FREEZE_PAY_TYPE))
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointOpType.FREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(number)
                .setDeltaCost(number / 2)
                .setPoint(super.point.getPoint()).setAvailable(INCREASE_POINT_1 - number)
                .setUsed(0L).setFrozen(number)
                .setExpire(0L)
                .setRefund(0L)
                .setCost(INCREASE_COST_1)
                .setFrozenCost(number / 2)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
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
                .setTotal(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1 - number)
                .setUsed(0L).setFrozen(number).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(number / 2)
                .setUsedCost(0L)
                .setRefundCost(0L)
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
        listPointRecLogParam.setRecId(pointRec.getId())
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assertions.assertEquals(2, pointRecLogList.size()); // 0: freeze; 1: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.FREEZE.code()).setDelta(number)
                .setAvailable(INCREASE_POINT_1 - number)
                .setUsed(0L)
                .setFrozen(number).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(number / 2)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);

        log.info("<< test end");
    }


    /**
     * 1, increase 100
     * 2, increase 200
     * 3, freeze 180
     */
    @Test
    public void test_freezePoint_2rec() throws InterruptedException {
        log.info(">> test start");
        long number = 180L;
        var point = super.increase2(super.point);

        long resultAvailable = point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number)
                .setOrderNo(UUID.randomUUID().toString()).setTag(USAGE_TAG)
                .setSourceType(FREEZE_SOURCE_TYPE).setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote("test_freezePoint");
        PointPo result = this.pointFreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(point.getPoint().longValue(), result.getPoint().longValue());
        Assertions.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(number, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setSourceTypes(List.of(FREEZE_SOURCE_TYPE))
                .setOrderTypes(List.of(FREEZE_ORDER_TYPE))
                .setPayTypes(List.of(FREEZE_PAY_TYPE))
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assertions.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointOpType.FREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(number)
                .setDeltaCost(number / 2)
                .setPoint(INCREASE_POINT_1 + INCREASE_POINT_2)
                .setAvailable(INCREASE_POINT_1 + INCREASE_POINT_2 - number)
                .setUsed(0L).setFrozen(number)
                .setExpire(0L)
                .setRefund(0L)
                .setCost(INCREASE_COST_1 + INCREASE_COST_2)
                .setFrozenCost(number / 2)
                .setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType())
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)));
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assertions.assertEquals(2, pointRecList.size());
        PointRecPo pointRec1 = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.point.getSeqNum())
                .setTotal(INCREASE_POINT_1).setAvailable(0L)
                .setUsed(0L).setFrozen(INCREASE_POINT_1).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(super.INCREASE_COST_1)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setTag(super.INCREASE_TAG_1)
                .setOrderNo(super.INCREASE_ORDER_NO_1)
                .setSourceType(super.INCREASE_SOURCE_TYPE)
                .setOrderType(INCREASE_ORDER_TYPE)
                .setPayType(INCREASE_PAY_TYPE);
        HyenaTestAssert.assertEquals(expectPointRec, pointRec1);

        PointRecPo pointRec2 = pointRecList.get(1);
        var expectPointRec2 = new PointRecPo();
        expectPointRec2.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease2)
                .setTotal(INCREASE_POINT_2).setAvailable(INCREASE_POINT_1 + INCREASE_POINT_2 - number)
                .setUsed(0L).setFrozen(number - INCREASE_POINT_1)
                .setExpire(0L).setCancelled(0L)
                .setTotalCost(INCREASE_COST_2)
                .setFrozenCost(40L) // 180 / 2 - 50
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setTag(INCREASE_TAG_2)
                .setOrderNo(INCREASE_ORDER_NO_2);
        HyenaTestAssert.assertEquals(expectPointRec2, pointRec2);

        // verify point record-1 logs
        var listPointRecLogParam = new ListPointRecLogParam();
        listPointRecLogParam.setRecId(pointRec1.getId())
                .setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        var pointRecLogList1 = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList1 = {}", pointRecLogList1);
        Assertions.assertEquals(2, pointRecLogList1.size()); // 0: freeze; 1: increase
        var pointRecLog11 = pointRecLogList1.get(0);
        var expectPointRecLog11 = new PointRecLogPo();
        expectPointRecLog11.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec1.getId())
                .setType(PointOpType.FREEZE.code()).setDelta(INCREASE_POINT_1)
                .setAvailable(0L)
                .setUsed(0L)
                .setFrozen(INCREASE_POINT_1).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(INCREASE_COST_1)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog11, pointRecLog11);

        // verify point record-2 logs
        listPointRecLogParam = new ListPointRecLogParam();
        listPointRecLogParam.setRecId(pointRec2.getId())
                .setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        var pointRecLogList2 = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList2 = {}", pointRecLogList2);
        Assertions.assertEquals(2, pointRecLogList2.size()); // 0: freeze; 1: increase
        var pointRecLog21 = pointRecLogList2.get(0);
        var expectPointRecLog21 = new PointRecLogPo();
        expectPointRecLog21.setUid(super.uid).setPid(point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec2.getId())
                .setType(PointOpType.FREEZE.code()).setDelta(number - INCREASE_POINT_1)
                .setAvailable(INCREASE_POINT_1 + INCREASE_POINT_2 - number)
                .setUsed(0L)
                .setFrozen(number - INCREASE_POINT_1).setCancelled(0L).setExpire(0L)
                .setCost(INCREASE_COST_2)
                .setFrozenCost(40L) // 180 / 2 - 50
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog21, pointRecLog21);

        log.info("<< test end");
    }
}
