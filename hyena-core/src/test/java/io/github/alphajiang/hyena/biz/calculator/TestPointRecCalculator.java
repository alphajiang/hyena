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

package io.github.alphajiang.hyena.biz.calculator;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
public class TestPointRecCalculator extends HyenaTestBase {

    @Autowired
    private PointBuilder pointBuilder;

    @Autowired
    private PointRecCalculator calculator;

    private PointRecPo rec;

    @BeforeEach
    public void init() {
        this.rec = new PointRecPo();
        this.rec.setUsed(DecimalUtils.ZERO)
                .setUsedCost(DecimalUtils.ZERO)
                .setFrozenCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setEnable(true)
                .setId(new Random().nextLong());
    }


    @Test
    public void test_freezePoint_no_enough_point() {
        Assertions.assertThrows(HyenaNoPointException.class, () -> {
            rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                    .setAvailable(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                    .setFrozen(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                    .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                    .setFrozenCost(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2));
            calculator.freezePoint(rec, BigDecimal.valueOf(200).setScale(DecimalUtils.SCALE_2));
        });
    }

    @Test
    public void test_freezePoint_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(DecimalUtils.ZERO)
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO);
        PointRecCalcResult result = calculator.freezePoint(rec, BigDecimal.valueOf(30L));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                rec.getAvailable() );
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                result.getRec4Update().getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), rec.getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2), rec.getFrozenCost() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getFrozenCost() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost() );
    }

    @Test
    public void test_freezePoint() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(5L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.freezePoint(rec, BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                rec.getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                result.getRec4Update().getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), // 10 + 20
                rec.getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2),
                result.getRec4Update().getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2),
                rec.getFrozenCost() );
        Assertions.assertEquals(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2),
                result.getRec4Update().getFrozenCost() );
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2),
                result.getDeltaCost() );
    }


    @Test
    public void test_unfreezePoint_no_enough_point() {
        Assertions.assertThrows(HyenaNoPointException.class, () -> {
            rec.setTotal(BigDecimal.valueOf(200L))
                    .setAvailable(BigDecimal.valueOf(100L)).setFrozen(BigDecimal.valueOf(30L))
                    .setTotalCost(BigDecimal.valueOf(100L))
                    .setFrozenCost(BigDecimal.valueOf(15L));
            calculator.unfreezePoint(rec, BigDecimal.valueOf(40), null);
        });
    }

    @Test
    public void test_unfreezePoint_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.unfreezePoint(rec, BigDecimal.valueOf(30L), null);
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(BigDecimal.valueOf(130L).setScale(DecimalUtils.SCALE_2),   // 100 + 15
                rec.getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(130L).setScale(DecimalUtils.SCALE_2),   // 100 + 15
                result.getRec4Update().getAvailable() );
        Assertions.assertEquals(DecimalUtils.ZERO, rec.getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getRec4Update().getFrozen());
        Assertions.assertEquals(DecimalUtils.ZERO, rec.getFrozenCost());
        Assertions.assertEquals(DecimalUtils.ZERO, result.getRec4Update().getFrozenCost());
        Assertions.assertEquals(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
    }

    @Test
    public void test_unfreezePoint() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.unfreezePoint(rec, BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2), null);
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(BigDecimal.valueOf(120L).setScale(DecimalUtils.SCALE_2),   // 100 + 15
                rec.getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(120L).setScale(DecimalUtils.SCALE_2),   // 100 + 15
                result.getRec4Update().getAvailable() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2), // 60 - 30
                rec.getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getFrozen() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), rec.getFrozenCost() );
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getFrozenCost());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
    }

    @Test
    public void test_decreasePoint_no_enough_point() {
        Assertions.assertThrows(HyenaNoPointException.class, () -> {
            rec.setTotal(BigDecimal.valueOf(200L))
                    .setAvailable(BigDecimal.valueOf(100L)).setFrozen(BigDecimal.valueOf(30L))
                    .setTotalCost(BigDecimal.valueOf(100L))
                    .setFrozenCost(BigDecimal.valueOf(15L));
            calculator.decreasePoint(rec, BigDecimal.valueOf(200));
        });
    }

    @Test
    public void test_decreasePoint_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setUsed(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.decreasePoint(rec, BigDecimal.valueOf(30L));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                rec.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
        Assertions.assertTrue(rec.getEnable());
        Assertions.assertTrue(result.getRec4Update().getEnable());
    }

    @Test
    public void test_decreasePoint_use_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setUsed(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(DecimalUtils.ZERO)
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.decreasePoint(rec, BigDecimal.valueOf(30L));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                rec.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
        Assertions.assertFalse(rec.getEnable());
        Assertions.assertFalse(result.getRec4Update().getEnable());
    }


    @Test
    public void test_decreasePoint() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setUsed(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(5L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.decreasePoint(rec, BigDecimal.valueOf(20).setScale(DecimalUtils.SCALE_2));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                rec.getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), // 10 + 20
                rec.getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsed());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
    }


    @Test
    public void test_cancelPoint_no_enough_point() {
        Assertions.assertThrows(HyenaNoPointException.class, () -> {
            rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                    .setAvailable(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                    .setFrozen(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                    .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                    .setFrozenCost(BigDecimal.valueOf(15L).setScale(DecimalUtils.SCALE_2));
            calculator.cancelPoint(rec, BigDecimal.valueOf(200).setScale(DecimalUtils.SCALE_2));
        });
    }

    @Test
    public void test_cancelPoint_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setCancelled(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.cancelPoint(rec, BigDecimal.valueOf(30L));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                rec.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
        Assertions.assertTrue(rec.getEnable());
        Assertions.assertTrue(result.getRec4Update().getEnable());
    }

    @Test
    public void test_cancelPoint_use_all() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setCancelled(BigDecimal.valueOf(20L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(DecimalUtils.ZERO)
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.cancelPoint(rec, BigDecimal.valueOf(30L));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                rec.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO,   //
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
        Assertions.assertFalse(rec.getEnable());
        Assertions.assertFalse(result.getRec4Update().getEnable());
    }


    @Test
    public void test_cancelPoint() {
        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setCancelled(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(5L).setScale(DecimalUtils.SCALE_2));
        PointRecCalcResult result = calculator.cancelPoint(rec, BigDecimal.valueOf(20).setScale(DecimalUtils.SCALE_2));
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                rec.getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2),   // 80 - 20
                result.getRec4Update().getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), // 10 + 20
                rec.getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getCancelled());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), rec.getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getRec4Update().getUsedCost());
        Assertions.assertEquals(BigDecimal.valueOf(10L).setScale(DecimalUtils.SCALE_2), result.getDeltaCost());
    }

    @Test
    public void test_refundPoint() {
        BigDecimal refund = BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2);
        BigDecimal cost = BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2);
        rec.setAvailable(BigDecimal.valueOf(150L).setScale(DecimalUtils.SCALE_2))
                .setRefund(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO)
                .setFrozen(DecimalUtils.ZERO);
        PointRecPo result = this.calculator.refundPoint(rec, refund, cost);
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getId());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), // 150 - 100
                rec.getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), // 150 - 100
                result.getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2), rec.getRefund());
        Assertions.assertEquals(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2), result.getRefund());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getRefundCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRefundCost());
        Assertions.assertTrue(rec.getEnable());
        Assertions.assertTrue(result.getEnable());

        rec.setAvailable(BigDecimal.valueOf(100L))
                .setRefund(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO);
        result = this.calculator.refundPoint(rec, refund, cost);
        log.info("result = {}", result);
        Assertions.assertEquals(rec.getId(), result.getId());
        Assertions.assertEquals(DecimalUtils.ZERO, // 150 - 100
                rec.getAvailable());
        Assertions.assertEquals(DecimalUtils.ZERO, // 150 - 100
                result.getAvailable());
        Assertions.assertEquals(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2), rec.getRefund());
        Assertions.assertEquals(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2), result.getRefund());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), rec.getRefundCost());
        Assertions.assertEquals(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2), result.getRefundCost());
        Assertions.assertFalse(rec.getEnable());
        Assertions.assertFalse(result.getEnable());
    }
}
