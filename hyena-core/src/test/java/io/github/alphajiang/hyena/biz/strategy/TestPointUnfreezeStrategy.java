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
import io.github.alphajiang.hyena.model.type.PointStatus;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaTestAssert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
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
     * point 100, available 20, frozen 80
     * unfreeze 50
     */
    @Test
    public void test_unfreezePoint() throws InterruptedException {
        log.info(">> test start");

        long freezeNum = 80L;
        long unfreezeNumber = 50L;
        long expectFrozen = 30L;    //freezeNum - unfreezeNumber
        long expectAvailable = 70L; //this.point.getPoint() - freezeNum + unfreezeNumber;

        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(freezeNum).setNote("test_unfreezePoint");
        this.point = this.pointFreezeStrategy.process(usage);

        usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setTag(USAGE_TAG).setOrderNo(UUID.randomUUID().toString())
                .setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assert.assertEquals(expectAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(expectFrozen, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(200L);

        // verify point log
        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setUid(this.uid).setSeqNum(result.getSeqNum())
                .setType(super.getPointType());
        var pointLogs = pointLogDs.listPointLog(listPointLogParam);
        log.info("pointLogs = {}", pointLogs);
        Assert.assertEquals(1, pointLogs.size());
        var pointLog = pointLogs.get(0);
        var expectPoingLog = new PointLog();
        expectPoingLog.setUid(this.uid).setType(PointStatus.UNFREEZE.code())
                .setSeqNum(result.getSeqNum()).setDelta(unfreezeNumber)
                .setPoint(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen)
                .setExpire(0L).setTag(usage.getTag())
                .setOrderNo(usage.getOrderNo())
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
        expectPointRec.setPid(super.point.getId()).setSeqNum(super.seqNumIncrease1)
                .setTotal(INCREASE_POINT_1).setAvailable(expectAvailable)
                .setUsed(0L).setFrozen(expectFrozen).setExpire(0L).setCancelled(0L)
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
        Assert.assertEquals(3, pointRecLogList.size()); // 0: unfreeze, 1, freeze; 2: increase
        var pointRecLog = pointRecLogList.get(0);
        var expectPointRecLog = new PointRecLogPo();
        expectPointRecLog.setUid(super.uid).setPid(super.point.getId())
                .setSeqNum(result.getSeqNum()).setRecId(pointRec.getId())
                .setType(PointStatus.UNFREEZE.code()).setDelta(unfreezeNumber)
                .setAvailable(expectAvailable)
                .setUsed(0L)
                .setFrozen(expectFrozen).setCancelled(0L).setExpire(0L)
                .setNote(usage.getNote());
        HyenaTestAssert.assertEquals(expectPointRecLog, pointRecLog);
        log.info("<< test end");
    }
}
