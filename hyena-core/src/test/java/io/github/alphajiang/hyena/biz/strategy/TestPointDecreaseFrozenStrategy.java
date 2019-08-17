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
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TestPointDecreaseFrozenStrategy extends TestPointStrategyBase {


    @Autowired
    private PointStrategy pointDecreaseFrozenStrategy;

    @Autowired
    private PointStrategy pointFreezeStrategy;

    @Autowired
    private PointStrategy pointIncreaseStrategy;

    @Before
    public void init() {
        super.init();

    }

    @Test
    public void test_decreasePointUnfreeze() {
        log.info(">> test start");

        log.info("point = {}", this.point);
        long unfreezeNumber = 80L;
        long useNumber = 60L;


        // 先冻结
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(unfreezeNumber)
                .setNote("test_decreasePointUnfreeze");
        PointPo retPoint = this.pointFreezeStrategy.process(usage);
        log.info("point = {}", retPoint);


        long resultNumber = retPoint.getPoint() - useNumber;
        long resultAvailable = retPoint.getAvailable() + unfreezeNumber - useNumber;
        long resultFrozen = retPoint.getFrozen() - unfreezeNumber;


        usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(useNumber)
                .setUnfreezePoint(unfreezeNumber)
                .setNote("test_decreasePointUnfreeze");
        PointPo result = this.pointDecreaseFrozenStrategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(resultNumber, result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(useNumber, result.getUsed().longValue());
        Assert.assertEquals(resultFrozen, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());
        log.info("<< test end");
    }
}
