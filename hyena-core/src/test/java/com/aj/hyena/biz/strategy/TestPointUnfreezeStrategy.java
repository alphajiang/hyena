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

import com.aj.hyena.biz.point.PointUsage;
import com.aj.hyena.biz.point.strategy.PointStrategy;
import com.aj.hyena.model.po.PointPo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestPointUnfreezeStrategy extends TestPointStrategyBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointIncreaseStrategy.class);

    @Autowired
    private PointStrategy pointFreezeStrategy;

    @Autowired
    private PointStrategy pointUnfreezeStrategy;

    @Test
    public void test_unfreezePoint() {
        long freezeNum = 80L;
        long unfreezeNumber = 50L;
        long resultFrozen = freezeNum - unfreezeNumber;
        long resultAvailable = this.point.getPoint() - freezeNum + unfreezeNumber;


        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setCusId(this.cusId).setPoint(80L).setNote("test_unfreezePoint");
        this.point = this.pointFreezeStrategy.process(usage);
        
        usage = new PointUsage();
        usage.setType(super.getPointType()).setCusId(this.cusId).setPoint(unfreezeNumber).setNote("test_unfreezePoint");
        PointPo result = this.pointUnfreezeStrategy.process(usage);
        logger.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue(), result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(resultFrozen, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }
}
