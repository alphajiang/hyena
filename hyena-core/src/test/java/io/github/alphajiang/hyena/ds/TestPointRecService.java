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
import io.github.alphajiang.hyena.ds.service.PointRecService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

public class TestPointRecService extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointRecService.class);


    @Autowired
    private PointRecService pointRecService;

    @Before
    public void init() {
        super.init();
    }

    @Test
    public void test_decreasePointUnfreeze() {
        long pointId = super.getUserPoint().getId();
        // 增加并冻结第一笔积分
        var pointRec = pointRecService.addPointRec(super.getPointType(), pointId, 10, "test", "{\"abc\" : 123, \"def\" : \"jkl\"}", null, null);
        var recA = pointRecService.getById(super.getPointType(), pointRec.getId(), false);
        logger.info("recA = {}", recA);
        pointRecService.freezePoint(super.getPointType(), recA, 20, null);

        recA = pointRecService.getById(super.getPointType(), pointRec.getId(), false);
        pointRecService.decreasePointUnfreeze(super.getPointType(), recA, 20, null);
    }


    @Test
    public void test_unfreezePoint() {
        long pointId = super.getUserPoint().getId();
        // 增加并冻结第一笔积分
        var pointRec = pointRecService.addPointRec(super.getPointType(), pointId, 10, "test", "{\"abc\" : 123}", null, null);
        var recA = pointRecService.getById(super.getPointType(), pointRec.getId(), false);
        pointRecService.freezePoint(super.getPointType(), recA, 20, null);

        recA = pointRecService.getById(super.getPointType(), pointRec.getId(), false);
        pointRecService.unfreezePoint(super.getPointType(), recA, 20, null);

    }

    @Test
    public void test_getIncreasedPoint() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);
        long increased = this.pointRecService.getIncreasedPoint(super.getPointType(),
                super.getUid(), start.getTime(), end.getTime());
        logger.info("increased = {}", increased);
        Assert.assertEquals(super.getUserPoint().getPoint().longValue(), increased);
    }
}
