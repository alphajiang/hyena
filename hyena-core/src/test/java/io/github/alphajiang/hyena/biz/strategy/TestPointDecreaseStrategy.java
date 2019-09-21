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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Slf4j
public class TestPointDecreaseStrategy extends TestPointStrategyBase {


    private final String USAGE_TAG = "TAG_TestPointDecreaseStrategy";
    @Autowired
    private PointStrategy pointDecreaseStrategy;


    @Autowired
    private PointStrategy pointIncreaseStrategy;


    /**
     * 1, increase 100
     * 2, decrease 80
     */
    @Test
    public void test_decreasePoint() throws InterruptedException {
        log.info(">> test start");
        long number = 80L;
        long resultNumber = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number)
                .setOrderNo(UUID.randomUUID().toString()).setTag(USAGE_TAG)
                .setSourceType(DECREASE_SOURCE_TYPE).setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setNote("test_decreasePoint");
        PointPo result = this.pointDecreaseStrategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(number, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setSourceTypes(List.of(DECREASE_SOURCE_TYPE))
                .setOrderTypes(List.of(DECREASE_ORDER_TYPE))
                .setPayTypes(List.of(DECREASE_PAY_TYPE))
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assert.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointOpType.DECREASE.code())
                .setSeqNum(result.getSeqNum()).setDelta(number)
                .setPoint(result.getPoint()).setAvailable(result.getAvailable())
                .setUsed(result.getUsed()).setFrozen(result.getFrozen())
                .setExpire(result.getExpire()).setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setExtra(usage.getExtra())
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPoingLog, pointLog);

        // verify point record
        ListPointRecParam listPointRecParam = new ListPointRecParam();
        listPointRecParam.setUid(this.uid).setType(super.getPointType());
        var pointRecList = pointRecDs.listPointRec(super.getPointType(), listPointRecParam);
        log.info("pointRecList = {}", pointRecList);
        Assert.assertEquals(1, pointRecList.size());
        PointRecPo pointRec = pointRecList.get(0);
        var expectPointRec = new PointRecPo();
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.point.getSeqNum())
                .setTotal(INCREASE_POINT_1).setAvailable(INCREASE_POINT_1 - number)
                .setUsed(number).setFrozen(0L).setExpire(0L).setCancelled(0L)
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
        listPointRecLogParam.setRecId(pointRec.getId())
                .setSorts(List.of(pointRecLogSortParam));
        var pointRecLogList = pointRecLogDs.listPointRecLog(super.getPointType(), listPointRecLogParam);
        log.info("pointRecLogList = {}", pointRecLogList);
        Assert.assertEquals(2, pointRecLogList.size()); // 0: decrease; 1: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointOpType.DECREASE.code()).setDelta(number)
                .setAvailable(pointRec.getAvailable())
                .setUsed(pointRec.getUsed())
                .setFrozen(0L).setCancelled(0L).setExpire(0L)
                .setSourceType(DECREASE_SOURCE_TYPE)
                .setOrderType(DECREASE_ORDER_TYPE)
                .setPayType(DECREASE_PAY_TYPE)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }


    @Test
    public void test_decreasePoint2() {
        log.info(">> test start");

        PointUsage increaseUsage = new PointUsage();
        increaseUsage.setType(super.getPointType()).setUid(this.uid).setPoint(555L).setNote("test_decreasePoint2");
        super.point = this.pointIncreaseStrategy.process(increaseUsage);


        long number = 123L;
        long resultNumber = this.point.getPoint() - number;

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number).setNote("test_decreasePoint2");

        PointPo result = this.pointDecreaseStrategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(number, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
        log.info("<< test end");
    }

    @Test
    public void test_decreasePoint_not_enough() {
        log.info(">> test start");
        PointUsage usage = new PointUsage();
        long number = 9999999L;
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number).setNote("test_decreasePoint_not_enough");
        PointPo result = this.pointDecreaseStrategy.process(usage);
        Assert.assertTrue(result.getAvailable() < 0L);
        //Assert.fail();
        log.info("<< test end");
    }
}
