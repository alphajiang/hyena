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

package com.aj.hyena.ds;

import com.aj.hyena.HyenaTestBase;
import com.aj.hyena.ds.service.PointService;
import com.aj.hyena.model.po.PointPo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class TestPointService extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointService.class);

    @Autowired
    private PointService pointService;

    private String cusId = "";
    private PointPo point;

    @Before
    public void init() {
        cusId = UUID.randomUUID().toString().substring(0, 4);
        long number = 100;
        this.point = this.pointService.increasePoint(super.getPointType(), cusId, number);
        logger.info("point = {}", point);
        Assert.assertEquals(number, point.getPoint().longValue());
        Assert.assertEquals(number, point.getAvailable().longValue());
        Assert.assertEquals(0L, point.getUsed().longValue());
        Assert.assertEquals(0L, point.getFrozen().longValue());
        Assert.assertEquals(0L, point.getExpire().longValue());
        Assert.assertEquals(true, point.getEnable().booleanValue());
    }

    @Test
    public void test_increasePoint_twice() {
        long number = 123L;
        long resultNumber = this.point.getPoint() + number;
        PointPo result = this.pointService.increasePoint(super.getPointType(), cusId, number);
        logger.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }

    @Test
    public void test_decreasePoint() {
        long number = 80L;
        long resultNumber = this.point.getPoint() - number;
        PointPo result = this.pointService.decreasePoint(super.getPointType(), cusId, number, "test_decreasePoint");
        logger.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(number, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }

    @Test
    public void test_decreasePoint2() {
        this.point = this.pointService.increasePoint(super.getPointType(), cusId, 555L);

        long number = 123L;
        long resultNumber = this.point.getPoint() - number;
        PointPo result = this.pointService.decreasePoint(super.getPointType(), cusId, number, "test_decreasePoint2");
        logger.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(number, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }

    @Test
    public void test_decreasePointUnfreeze() {
        long freezeNumber = 80L;
        long useNumber = 60L;
        long resultNumber = this.point.getPoint() - useNumber;
        long resultAvailable = this.point.getPoint() - freezeNumber;
        long resultFrozen = freezeNumber - useNumber;
        this.pointService.freezePoint(super.getPointType(), cusId, freezeNumber, "test_decreasePointUnfreeze");
        PointPo result = this.pointService.decreasePointUnfreeze(super.getPointType(), cusId, useNumber, "test_decreasePointUnfreeze");
        logger.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(useNumber, result.getUsed().longValue());
        Assert.assertEquals(resultFrozen, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }


    @Test
    public void test_freezePoint() {
        long number = 80L;
        long resultAvailable = this.point.getPoint() - number;
        PointPo result = this.pointService.freezePoint(super.getPointType(), cusId, number, "test_freezePoint");
        logger.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(number, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }

    @Test
    public void test_unfreezePoint() {
        long freezeNum = 80L;
        long unfreezeNumber = 50L;
        long resultFrozen = freezeNum - unfreezeNumber;
        long resultAvailable = this.point.getPoint() - freezeNum + unfreezeNumber;
        this.point = this.pointService.freezePoint(super.getPointType(), cusId, 80L, "test_unfreezePoint");

        PointPo result = this.pointService.unfreezePoint(super.getPointType(), cusId, unfreezeNumber, "test_unfreezePoint");
        logger.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(resultFrozen, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }
}
