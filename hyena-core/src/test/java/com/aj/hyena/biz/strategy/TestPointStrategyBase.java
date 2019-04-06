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

package com.aj.hyena.biz.strategy;

import com.aj.hyena.HyenaTestBase;
import com.aj.hyena.biz.point.PointUsage;
import com.aj.hyena.biz.point.strategy.PointStrategy;
import com.aj.hyena.model.po.PointPo;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class TestPointStrategyBase extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointStrategyBase.class);

    @Autowired
    protected PointStrategy pointIncreaseStrategy;

    protected String cusId = "";
    protected PointPo point;

    @Before
    public void init() {
        super.init();
        cusId = UUID.randomUUID().toString().substring(0, 4);
        long number = 100;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setCusId(this.cusId).setPoint(100);
        this.point = this.pointIncreaseStrategy.process(usage);
        logger.info("point = {}", point);
        Assert.assertEquals(number, point.getPoint().longValue());
        Assert.assertEquals(number, point.getAvailable().longValue());
        Assert.assertEquals(0L, point.getUsed().longValue());
        Assert.assertEquals(0L, point.getFrozen().longValue());
        Assert.assertEquals(0L, point.getExpire().longValue());
        Assert.assertEquals(true, point.getEnable().booleanValue());
    }
}
