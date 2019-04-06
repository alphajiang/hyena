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

package com.aj.hyena.biz;

import com.aj.hyena.HyenaTestBase;
import com.aj.hyena.biz.point.PointUsage;
import com.aj.hyena.biz.point.PointUsageFacade;
import com.aj.hyena.model.po.PointPo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestPointUsageFacade extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointUsageFacade.class);

    @Autowired
    private PointUsageFacade pointUsageFacade;

//    private String cusId = "";
//    private PointPo point;
//
//    @Before
//    public void init() {
//        cusId = UUID.randomUUID().toString().substring(0, 4);
//        long number = 100;
//        PointUsage usage = new PointUsage();
//        usage.setType(super.getPointType()).setCusId(cusId).setPoint(number);
//        Optional<PointPo> ret = this.pointUsageFacade.increase(usage);
//        this.point = ret.get();
//        logger.info("point = {}", point);
//        Assert.assertEquals(number, point.getPoint().longValue());
//        Assert.assertEquals(number, point.getAvailable().longValue());
//        Assert.assertEquals(0L, point.getUsed().longValue());
//        Assert.assertEquals(0L, point.getFrozen().longValue());
//        Assert.assertEquals(0L, point.getExpire().longValue());
//        Assert.assertEquals(true, point.getEnable().booleanValue());
//    }

    @Test
    public void test_increase() {
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setCusId("test_increase").setPoint(99887L);
        PointPo ret = this.pointUsageFacade.increase(usage);
        logger.info("point = {}", ret);
        Assert.assertNotNull(ret);

    }

//    @Test
//    public void test_decrease() {
//        PointUsage usage = new PointUsage();
//        usage.setType(super.getPointType()).setCusId("test_decrease").setPoint(99887L);
//        Optional<PointPo> ret = this.pointUsageFacade.decrease(usage);
//        ret.ifPresentOrElse(obj -> logger.info("point = {}", obj), () -> {
//            logger.error("decrease failed!!!!");
//            Assert.fail();
//        });
//    }
}
