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
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestPointCancelStrategy extends TestPointStrategyBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointCancelStrategy.class);


    @Autowired
    private PointStrategy pointCancelStrategy;

    @Autowired
    private PointRecService pointRecService;

    @Test
    public void test_cancelPoint() {
        ListPointRecParam param = new ListPointRecParam();
        param.setCusId(super.cusId).setStart(0L).setSize(1);
        List<PointRecPo> recList = this.pointRecService.listPointRec(super.getPointType(), param);
        PointRecPo rec = recList.get(0);

        long number = rec.getAvailable();
        long resultAvailable = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setRecId(rec.getId())
                .setCusId(this.cusId).setPoint(number).setNote("test_cancelPoint");
        PointPo result = this.pointCancelStrategy.process(usage);
        logger.info("result = {}", result);
        Assert.assertEquals(this.point.getPoint().longValue() - number, result.getPoint().longValue());
        Assert.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assert.assertEquals(0L, result.getUsed().longValue());
        Assert.assertEquals(0L, result.getFrozen().longValue());
        Assert.assertEquals(0L, result.getExpire().longValue());

        PointRecPo resultRec = this.pointRecService.getById(super.getPointType(), rec.getId(), false);
        logger.info("resultRec = {}", resultRec);
        Assert.assertFalse(resultRec.getEnable());
        Assert.assertTrue(resultRec.getAvailable().longValue() == 0L);
        Assert.assertTrue(resultRec.getCancelled().longValue() == number);
    }
}
