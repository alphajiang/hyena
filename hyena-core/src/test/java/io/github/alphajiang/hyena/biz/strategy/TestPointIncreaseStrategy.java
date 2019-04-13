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
import io.github.alphajiang.hyena.model.po.PointPo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestPointIncreaseStrategy extends TestPointStrategyBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointIncreaseStrategy.class);

    @Autowired
    private PointStrategy pointIncreaseStrategy;


    @Test
    public void test_increasePoint_twice() {
        long number = 123L;
        long resultNumber = this.point.getPoint() + number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(number);
        PointPo result = this.pointIncreaseStrategy.process(usage);
        logger.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultNumber, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
    }
}
