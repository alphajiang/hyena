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

package io.github.alphajiang.hyena.biz;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.model.po.PointPo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class TestPointUsageFacade extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointUsageFacade.class);

    @Autowired
    private PointUsageFacade pointUsageFacade;


    @Test
    public void test_increase() {
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid("test_increase")
                .setPoint(BigDecimal.valueOf(99887L));
        PointPo ret = this.pointUsageFacade.increase(usage);
        logger.info("point = {}", ret);
        Assertions.assertNotNull(ret);

    }


}
