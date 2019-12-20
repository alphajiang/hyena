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

package io.github.alphajiang.hyena.ds;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestPointRecDs extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointRecDs.class);


    @Autowired
    private PointRecDs pointRecDs;

    @BeforeEach
    public void init() {
        super.init();
    }

//    @Test
//    public void test_decreasePointUnfreeze() {
//        long pointId = super.getUserPoint().getId();
//        // 增加并冻结第一笔积分
//        PointUsage param = new PointUsage();
//        param.setType(super.getPointType()).setPoint(10).setTag("test")
//                .setExtra("{\"abc\" : 123, \"def\" : \"jkl\"}")
//                .setNote(null)
//                .setExpireTime(null);
//        var pointRec = pointRecDs.addPointRec(param, pointId, super.getUserPoint().getSeqNum());
//        var recA = pointRecDs.getById(super.getPointType(), pointRec.getId(), false);
//        logger.info("recA = {}", recA);
//        pointRecDs.freezePoint(super.getPointType(), recA, 20, null);
//
//        recA = pointRecDs.getById(super.getPointType(), pointRec.getId(), false);
//        pointRecDs.decreasePointUnfreeze(super.getPointType(), recA,  super.getUserPoint().getSeqNum(), 20, null);
//    }




    @Test
    public void test_getIncreasedPoint() throws InterruptedException {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);
        Thread.sleep(100L);
        BigDecimal increased = this.pointRecDs.getIncreasedPoint(super.getPointType(),
                super.getUid(), start.getTime(), end.getTime());
        logger.info("increased = {}", increased);
        Assertions.assertEquals(super.getUserPoint().getPoint() , increased);
    }

    @Test
    public void test_batchInsert() {
        List<PointRecPo> list = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            PointRecPo rec = new PointRecPo();

            rec.setPid(i + 1)
                    .setSeqNum(45L)
                    .setTotal(BigDecimal.valueOf(999L))
                    .setTotalCost(BigDecimal.valueOf(998L))
                    .setAvailable(BigDecimal.valueOf(111L))
                    .setUsed(BigDecimal.valueOf(222L))
                    .setFrozen(BigDecimal.valueOf(333L))
                    .setExpire(BigDecimal.valueOf(444L))
                    .setRefund(BigDecimal.valueOf(555L))
                    .setCancelled(BigDecimal.valueOf(666L))
                    .setFrozenCost(BigDecimal.valueOf(777L))
                    .setUsedCost(BigDecimal.valueOf(888L))
                    .setRefundCost(BigDecimal.valueOf(999L))
                    .setExtra("abcd1234")
                    .setIssueTime(new Date())
                    .setTag("ddddd")
                    .setOrderNo("eeeee")
                    .setSourceType(3)
                    .setOrderType(4)
                    .setPayType(5)
                    .setExpireTime(new Date());

            list.add(rec);
        }
        this.pointRecDs.batchInsert(super.getPointType(), list);
    }

    @Test
    public void test_batchUpdate() {
        List<PointRecPo> list = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            PointRecPo rec = new PointRecPo();
            if (i % 2 == 1) {
                rec.setId(i + 1);
                rec.setAvailable(BigDecimal.valueOf(111L))
                        .setUsed(BigDecimal.valueOf(222L))
                        .setFrozen(BigDecimal.valueOf(333L))
                        .setExpire(BigDecimal.valueOf(444L))
                        .setRefund(BigDecimal.valueOf(555L))
                        .setCancelled(BigDecimal.valueOf(666L))
                        .setFrozenCost(BigDecimal.valueOf(777L))
                        .setUsedCost(BigDecimal.valueOf(888L))
                        .setRefundCost(BigDecimal.valueOf(999L))
                        .setExtra("abcd1234")
                        .setIssueTime(new Date())
                        .setEnable(false);
            } else {
                rec.setPid(11L).setSeqNum(45L);
            }
            list.add(rec);
        }
        this.pointRecDs.batchUpdate(super.getPointType(), list);
    }
}
