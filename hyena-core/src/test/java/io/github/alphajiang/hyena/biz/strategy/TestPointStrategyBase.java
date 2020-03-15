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
import io.github.alphajiang.hyena.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
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

    protected final BigDecimal INCREASE_POINT_1 = BigDecimal.valueOf(100).setScale(DecimalUtils.SCALE_2);
    protected final BigDecimal INCREASE_COST_1 = BigDecimal.valueOf(50).setScale(DecimalUtils.SCALE_2);
    protected final String INCREASE_TAG_1 = "TAG_" + UUID.randomUUID().toString();
    protected final String INCREASE_ORDER_NO_1 = "ORDER_NO_" + UUID.randomUUID().toString();


    protected final BigDecimal INCREASE_POINT_2 = BigDecimal.valueOf(200).setScale(DecimalUtils.SCALE_2);
    protected final BigDecimal INCREASE_COST_2 = BigDecimal.valueOf(100).setScale(DecimalUtils.SCALE_2);
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


    @BeforeEach
    public void init() {
        super.init();
        uid = UUID.randomUUID().toString().substring(0, 4);

        increase1();


        this.tcInit();
    }


    public void tcInit() {

    }

    @Override
    public String getUid() {
        return this.uid;
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
        Assertions.assertEquals(INCREASE_POINT_1, point.getPoint());
        Assertions.assertEquals(INCREASE_POINT_1, point.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, point.getUsed());
        Assertions.assertEquals(DecimalUtils.ZERO, point.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, point.getExpire());
        Assertions.assertEquals(true, point.getEnable().booleanValue());
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
        Assertions.assertEquals(INCREASE_POINT_2.add(beforeIncrease.getPoint()), result.getPoint());
        Assertions.assertEquals(INCREASE_POINT_2.add(beforeIncrease.getPoint()), result.getAvailable());
        Assertions.assertEquals(beforeIncrease.getUsed(), result.getUsed());
        Assertions.assertEquals(beforeIncrease.getFrozen(), result.getFrozen());
        Assertions.assertEquals(beforeIncrease.getExpire(), result.getExpire());
        Assertions.assertEquals(true, result.getEnable().booleanValue());
        this.seqNumIncrease2 = result.getSeqNum();
        try {
            Thread.sleep(100L);
        } catch (Exception e) {
            log.error("error = {}", e.getMessage(), e);
        }
        return result;
    }
}
