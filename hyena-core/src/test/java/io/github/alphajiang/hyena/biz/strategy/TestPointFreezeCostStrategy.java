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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        long number = 20L;
        long resultAvailable = 60L; // 100 - 20*2
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setCost(number)
                .setOrderNo(UUID.randomUUID().toString()).setTag(USAGE_TAG)
                .setSourceType(FREEZE_SOURCE_TYPE).setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote("test_freezeCost");
        PointPo result = this.strategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(40L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
        Assert.assertEquals(20L, result.getFrozenCost().longValue());

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
        Assert.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPointLog = new PointLog();
        expectPointLog.setUid(this.uid).setType(PointOpType.FREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(number * 2)
                .setDeltaCost(number)
                .setPoint(super.INCREASE_POINT_1)
                .setAvailable(INCREASE_POINT_1 - number * 2)
                .setUsed(0L).setFrozen(number * 2)
                .setExpire(0L)
                .setRefund(0L)
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
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assert.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1 - number * 2)
                .setUsed(0L).setFrozen(40L).setExpire(0L).setCancelled(0L)
                .setTotalCost(super.INCREASE_COST_1)
                .setFrozenCost(20L)
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
        Assert.assertEquals(2, pointRecLogList.size()); // 0: freeze; 1: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.FREEZE.code()).setDelta(number * 2)
                .setAvailable(INCREASE_POINT_1 - number * 2)
                .setUsed(0L)
                .setFrozen(number * 2).setCancelled(0L).setExpire(0L)
                .setCost(super.INCREASE_COST_1)
                .setFrozenCost(number)
                .setUsedCost(0L)
                .setRefundCost(0L)
                .setSourceType(FREEZE_SOURCE_TYPE)
                .setOrderType(FREEZE_ORDER_TYPE)
                .setPayType(FREEZE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);

        log.info("<< test end");
    }
}
