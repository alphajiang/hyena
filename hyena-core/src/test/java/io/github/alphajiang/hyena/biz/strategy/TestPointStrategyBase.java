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

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointPo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Slf4j
public abstract class TestPointStrategyBase extends HyenaTestBase {
    protected final int INCREASE_SOURCE_TYPE = 12;
    protected final int INCREASE_ORDER_TYPE = 13;
    protected final int INCREASE_PAY_TYPE = 14;

    protected final int FREEZE_SOURCE_TYPE = 33;
    protected final int FREEZE_ORDER_TYPE = 34;
    protected final int FREEZE_PAY_TYPE = 35;

    protected final int DECREASE_SOURCE_TYPE = 63;
    protected final int DECREASE_ORDER_TYPE = 64;
    protected final int DECREASE_PAY_TYPE = 65;

    protected final long INCREASE_POINT_1 = 100L;
    protected final long INCREASE_COST_1 = 50L;
    protected final String INCREASE_TAG_1 = "TAG_" + UUID.randomUUID().toString();
    protected final String INCREASE_ORDER_NO_1 = "ORDER_NO_" + UUID.randomUUID().toString();


    protected final long INCREASE_POINT_2 = 200L;
    protected final long INCREASE_COST_2 = 100L;
    protected final String INCREASE_TAG_2 = "TAG_" + UUID.randomUUID().toString();
    protected final String INCREASE_ORDER_NO_2 = "ORDER_NO_" + UUID.randomUUID().toString();

    @Autowired
    protected PointStrategy pointIncreaseStrategy;

    @Autowired
    protected PointDs pointDs;

    @Autowired
    protected PointLogDs pointLogDs;

    @Autowired
    protected PointRecDs pointRecDs;

    @Autowired
    protected PointRecLogDs pointRecLogDs;
    protected String uid = "";
    protected PointPo point;
    protected long seqNumIncrease1;
    protected long seqNumIncrease2;


    @Before
    public void init() {
        super.init();
        uid = UUID.randomUUID().toString().substring(0, 4);

        increase1();
    }


    public void increase1() {
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(INCREASE_POINT_1)
                .setCost(INCREASE_COST_1)
                .setTag(INCREASE_TAG_1)
                .setOrderNo(INCREASE_ORDER_NO_1)
                .setSourceType(INCREASE_SOURCE_TYPE).setOrderType(INCREASE_ORDER_TYPE).setPayType(INCREASE_PAY_TYPE);
        var resultPoint = this.pointIncreaseStrategy.process(usage);
        this.point = new PointPo();
        BeanUtils.copyProperties(resultPoint, this.point);
        log.info("point = {}", point);
        Assert.assertEquals(INCREASE_POINT_1, point.getPoint().longValue());
        Assert.assertEquals(INCREASE_POINT_1, point.getAvailable().longValue());
        Assert.assertEquals(0L, point.getUsed().longValue());
        Assert.assertEquals(0L, point.getFrozen().longValue());
        Assert.assertEquals(0L, point.getExpire().longValue());
        Assert.assertEquals(true, point.getEnable().booleanValue());
        seqNumIncrease1 = this.point.getSeqNum();
        try {
            Thread.sleep(100L);
        } catch (Exception e) {
            log.error("error = {}", e.getMessage(), e);
        }
    }

    public PointPo increase2(PointPo beforeIncrease) {
        log.info(">>");
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setUid(this.uid).setPoint(INCREASE_POINT_2)
                .setTag(INCREASE_TAG_2)
                .setCost(INCREASE_COST_2)
                .setOrderNo(INCREASE_ORDER_NO_2);
        var result = this.pointIncreaseStrategy.process(usage);
        log.info("result = {}", result);
        Assert.assertEquals(INCREASE_POINT_2 + beforeIncrease.getPoint(), result.getPoint().longValue());
        Assert.assertEquals(INCREASE_POINT_2 + beforeIncrease.getPoint(), result.getAvailable().longValue());
        Assert.assertEquals(beforeIncrease.getUsed().longValue(), result.getUsed().longValue());
        Assert.assertEquals(beforeIncrease.getFrozen().longValue(), result.getFrozen().longValue());
        Assert.assertEquals(beforeIncrease.getExpire().longValue(), result.getExpire().longValue());
        Assert.assertEquals(true, result.getEnable().booleanValue());
        this.seqNumIncrease2 = result.getSeqNum();
        try {
            Thread.sleep(100L);
        } catch (Exception e) {
            log.error("error = {}", e.getMessage(), e);
        }
        return result;
    }
}
