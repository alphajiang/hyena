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

import io.github.alphajiang.hyena.biz.point.PSession;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class TestPointIncreaseStrategy extends TestPointStrategyBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointIncreaseStrategy.class);

    @Autowired
    private PointStrategy pointIncreaseStrategy;


    @Test
    public void test_increasePoint_twice() {
        BigDecimal number = BigDecimal.valueOf(123L).setScale(DecimalUtils.SCALE_2);
        BigDecimal resultNumber = this.point.getPoint().add(number);
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(super.getUid()).setPoint(number);
        PointPo result = this.pointIncreaseStrategy.process(PSession.fromUsage(usage))
                .block()
                .getResult();
        logger.info("result = {}", result);
        Assertions.assertEquals(resultNumber, result.getPoint());
        Assertions.assertEquals(resultNumber, result.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getUsed());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getExpire());
    }
}
