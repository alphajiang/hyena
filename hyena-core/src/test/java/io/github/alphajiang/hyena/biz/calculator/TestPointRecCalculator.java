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
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

@Slf4j
public class TestPointRecCalculator extends HyenaTestBase {

    @Autowired
    private PointRecCalculator calculator;
    private PointRecPo rec;

    @Before
    public void init() {
        this.rec = new PointRecPo();
        this.rec.setUsed(0L)
                .setUsedCost(0L)
                .setFrozenCost(0L)
                .setRefundCost(0L)
                .setEnable(true)
                .setId(new Random().nextLong());
    }


    @Test(expected = HyenaNoPointException.class)
    public void test_freezePoint_no_enough_point() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(30L)
                .setTotalCost(100L)
                .setFrozenCost(15L);
        calculator.freezePoint(rec, 200);
    }

    @Test
    public void test_freezePoint_all() {
        rec.setTotal(200L)
                .setAvailable(30L).setFrozen(0L)
                .setTotalCost(100L)
                .setUsedCost(40L)
                .setFrozenCost(0L);
        PointRecCalcResult result = calculator.freezePoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(0L,   //
                rec.getAvailable().longValue());
        Assert.assertEquals(0L,   //
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(30L, rec.getFrozen().longValue());
        Assert.assertEquals(30L, result.getRec4Update().getFrozen().longValue());
        Assert.assertEquals(60L, rec.getFrozenCost().longValue());
        Assert.assertEquals(60L, result.getRec4Update().getFrozenCost().longValue());
        Assert.assertEquals(60L, result.getDeltaCost().longValue());
    }

    @Test
    public void test_freezePoint() {
        rec.setTotal(200L)
                .setAvailable(80L).setFrozen(10L)
                .setTotalCost(100L)
                .setFrozenCost(5L);
        PointRecCalcResult result = calculator.freezePoint(rec, 20L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(60L,   // 80 - 20
                rec.getAvailable().longValue());
        Assert.assertEquals(60L,   // 80 - 20
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(30L, // 10 + 20
                rec.getFrozen().longValue());
        Assert.assertEquals(30L, result.getRec4Update().getFrozen().longValue());
        Assert.assertEquals(15L, rec.getFrozenCost().longValue());
        Assert.assertEquals(15L, result.getRec4Update().getFrozenCost().longValue());
        Assert.assertEquals(10L, result.getDeltaCost().longValue());
    }


    @Test(expected = HyenaNoPointException.class)
    public void test_unfreezePoint_no_enough_point() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(30L)
                .setTotalCost(100L)
                .setFrozenCost(15L);
        calculator.unfreezePoint(rec, 40);
    }

    @Test
    public void test_unfreezePoint_all() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(30L)
                .setTotalCost(100L)
                .setFrozenCost(15L);
        PointRecCalcResult result = calculator.unfreezePoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(130L,   // 100 + 15
                rec.getAvailable().longValue());
        Assert.assertEquals(130L,   // 100 + 15
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(0L, rec.getFrozen().longValue());
        Assert.assertEquals(0L, result.getRec4Update().getFrozen().longValue());
        Assert.assertEquals(0L, rec.getFrozenCost().longValue());
        Assert.assertEquals(0L, result.getRec4Update().getFrozenCost().longValue());
        Assert.assertEquals(15L, result.getDeltaCost().longValue());
    }

    @Test
    public void test_unfreezePoint() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(80L)
                .setTotalCost(100L)
                .setFrozenCost(40L);
        PointRecCalcResult result = calculator.unfreezePoint(rec, 20L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(120L,   // 100 + 15
                rec.getAvailable().longValue());
        Assert.assertEquals(120L,   // 100 + 15
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(60L, // 60 - 30
                rec.getFrozen().longValue());
        Assert.assertEquals(60L, result.getRec4Update().getFrozen().longValue());
        Assert.assertEquals(30L, rec.getFrozenCost().longValue());
        Assert.assertEquals(30L, result.getRec4Update().getFrozenCost().longValue());
        Assert.assertEquals(10L, result.getDeltaCost().longValue());
    }

    @Test(expected = HyenaNoPointException.class)
    public void test_decreasePoint_no_enough_point() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(30L)
                .setTotalCost(100L)
                .setFrozenCost(15L);
        calculator.decreasePoint(rec, 200);
    }

    @Test
    public void test_decreasePoint_all() {
        rec.setTotal(200L)
                .setAvailable(30L)
                .setUsed(20L)
                .setFrozen(10L)
                .setTotalCost(100L)
                .setUsedCost(40L)
                .setFrozenCost(10L);
        PointRecCalcResult result = calculator.decreasePoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(0L,   //
                rec.getAvailable().longValue());
        Assert.assertEquals(0L,   //
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(50L, rec.getUsed().longValue());
        Assert.assertEquals(50L, result.getRec4Update().getUsed().longValue());
        Assert.assertEquals(90L, rec.getUsedCost().longValue());
        Assert.assertEquals(90L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(50L, result.getDeltaCost().longValue());
        Assert.assertTrue(rec.getEnable());
        Assert.assertTrue(result.getRec4Update().getEnable());
    }

    @Test
    public void test_decreasePoint_use_all() {
        rec.setTotal(200L)
                .setAvailable(30L)
                .setUsed(20L)
                .setFrozen(0L)
                .setTotalCost(100L)
                .setUsedCost(40L)
                .setFrozenCost(10L);
        PointRecCalcResult result = calculator.decreasePoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(0L,   //
                rec.getAvailable().longValue());
        Assert.assertEquals(0L,   //
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(50L, rec.getUsed().longValue());
        Assert.assertEquals(50L, result.getRec4Update().getUsed().longValue());
        Assert.assertEquals(90L, rec.getUsedCost().longValue());
        Assert.assertEquals(90L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(50L, result.getDeltaCost().longValue());
        Assert.assertFalse(rec.getEnable());
        Assert.assertFalse(result.getRec4Update().getEnable());
    }


    @Test
    public void test_decreasePoint() {
        rec.setTotal(200L)
                .setAvailable(80L)
                .setUsed(10L)
                .setFrozen(10L)
                .setTotalCost(100L)
                .setFrozenCost(5L);
        PointRecCalcResult result = calculator.decreasePoint(rec, 20);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(60L,   // 80 - 20
                rec.getAvailable().longValue());
        Assert.assertEquals(60L,   // 80 - 20
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(30L, // 10 + 20
                rec.getUsed().longValue());
        Assert.assertEquals(30L, result.getRec4Update().getUsed().longValue());
        Assert.assertEquals(10L, rec.getUsedCost().longValue());
        Assert.assertEquals(10L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(10L, result.getDeltaCost().longValue());
    }


    @Test(expected = HyenaNoPointException.class)
    public void test_cancelPoint_no_enough_point() {
        rec.setTotal(200L)
                .setAvailable(100L).setFrozen(30L)
                .setTotalCost(100L)
                .setFrozenCost(15L);
        calculator.cancelPoint(rec, 200);
    }

    @Test
    public void test_cancelPoint_all() {
        rec.setTotal(200L)
                .setAvailable(30L)
                .setCancelled(20L)
                .setFrozen(10L)
                .setTotalCost(100L)
                .setUsedCost(40L)
                .setFrozenCost(10L);
        PointRecCalcResult result = calculator.cancelPoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(0L,   //
                rec.getAvailable().longValue());
        Assert.assertEquals(0L,   //
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(50L, rec.getCancelled().longValue());
        Assert.assertEquals(50L, result.getRec4Update().getCancelled().longValue());
        Assert.assertEquals(90L, rec.getUsedCost().longValue());
        Assert.assertEquals(90L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(50L, result.getDeltaCost().longValue());
        Assert.assertTrue(rec.getEnable());
        Assert.assertTrue(result.getRec4Update().getEnable());
    }

    @Test
    public void test_cancelPoint_use_all() {
        rec.setTotal(200L)
                .setAvailable(30L)
                .setCancelled(20L)
                .setFrozen(0L)
                .setTotalCost(100L)
                .setUsedCost(40L)
                .setFrozenCost(10L);
        PointRecCalcResult result = calculator.cancelPoint(rec, 30L);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(0L,   //
                rec.getAvailable().longValue());
        Assert.assertEquals(0L,   //
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(50L, rec.getCancelled().longValue());
        Assert.assertEquals(50L, result.getRec4Update().getCancelled().longValue());
        Assert.assertEquals(90L, rec.getUsedCost().longValue());
        Assert.assertEquals(90L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(50L, result.getDeltaCost().longValue());
        Assert.assertFalse(rec.getEnable());
        Assert.assertFalse(result.getRec4Update().getEnable());
    }


    @Test
    public void test_cancelPoint() {
        rec.setTotal(200L)
                .setAvailable(80L)
                .setCancelled(10L)
                .setFrozen(10L)
                .setTotalCost(100L)
                .setFrozenCost(5L);
        PointRecCalcResult result = calculator.cancelPoint(rec, 20);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getRec4Update().getId());
        Assert.assertEquals(60L,   // 80 - 20
                rec.getAvailable().longValue());
        Assert.assertEquals(60L,   // 80 - 20
                result.getRec4Update().getAvailable().longValue());
        Assert.assertEquals(30L, // 10 + 20
                rec.getCancelled().longValue());
        Assert.assertEquals(30L, result.getRec4Update().getCancelled().longValue());
        Assert.assertEquals(10L, rec.getUsedCost().longValue());
        Assert.assertEquals(10L, result.getRec4Update().getUsedCost().longValue());
        Assert.assertEquals(10L, result.getDeltaCost().longValue());
    }

    @Test
    public void test_refundPoint() {
        long refund = 100L;
        long cost = 50L;
        rec.setAvailable(150L)
                .setRefund(0L)
                .setRefundCost(0L)
                .setFrozen(0L);
        PointRecPo result = this.calculator.refundPoint(rec, refund, cost);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getId());
        Assert.assertEquals(50L, // 150 - 100
                rec.getAvailable().longValue());
        Assert.assertEquals(50L, // 150 - 100
                result.getAvailable().longValue());
        Assert.assertEquals(100L, rec.getRefund().longValue());
        Assert.assertEquals(100L, result.getRefund().longValue());
        Assert.assertEquals(50L, rec.getRefundCost().longValue());
        Assert.assertEquals(50L, result.getRefundCost().longValue());
        Assert.assertTrue(rec.getEnable());
        Assert.assertTrue(result.getEnable());

        rec.setAvailable(100L)
                .setRefund(0L)
                .setRefundCost(0L);
        result = this.calculator.refundPoint(rec, refund, cost);
        log.info("result = {}", result);
        Assert.assertEquals(rec.getId(), result.getId());
        Assert.assertEquals(0L, // 150 - 100
                rec.getAvailable().longValue());
        Assert.assertEquals(0L, // 150 - 100
                result.getAvailable().longValue());
        Assert.assertEquals(100L, rec.getRefund().longValue());
        Assert.assertEquals(100L, result.getRefund().longValue());
        Assert.assertEquals(50L, rec.getRefundCost().longValue());
        Assert.assertEquals(50L, result.getRefundCost().longValue());
        Assert.assertFalse(rec.getEnable());
        Assert.assertFalse(result.getEnable());
    }
}
