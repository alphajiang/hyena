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

import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestCostCalculator {


    private CostCalculator calculator;

    @BeforeEach
    public void setup() {
        calculator = new CostCalculator();
    }

    @Test
    public void test_getAvailableCost() {
        PointRecPo rec = new PointRecPo();
        rec.setTotalCost(DecimalUtils.ZERO);
        BigDecimal result = this.calculator.getAvailableCost(rec);
        Assertions.assertEquals(DecimalUtils.ZERO, result);

        rec.setTotalCost(BigDecimal.valueOf(100L))
                .setFrozenCost(BigDecimal.valueOf(5L))
                .setUsedCost(BigDecimal.valueOf(6L))
                .setRefundCost(BigDecimal.valueOf(7L));
        BigDecimal expect = BigDecimal.valueOf(100L - 5L - 6L - 7L);
        result = this.calculator.getAvailableCost(rec);
        Assertions.assertEquals(expect, result);
    }

    @Test
    public void test_accountPoint() {
        PointRecPo rec = new PointRecPo();
        BigDecimal cost = BigDecimal.valueOf(45).setScale(DecimalUtils.SCALE_2);
        rec.setAvailable(DecimalUtils.ZERO);
        BigDecimal result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(DecimalUtils.ZERO, result);

        rec.setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(80L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO)
                .setRefundCost(DecimalUtils.ZERO);
        result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(DecimalUtils.ZERO, result);

        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO).setRefundCost(DecimalUtils.ZERO);
        BigDecimal expect = BigDecimal.valueOf(90L).setScale(DecimalUtils.SCALE_2);  //  45 / 100 * 200
        result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(expect, result);
    }


    @Test
    public void test_accountCost() {
        PointRecPo rec = new PointRecPo();
        BigDecimal delta = BigDecimal.valueOf(45).setScale(DecimalUtils.SCALE_2);

        rec.setTotalCost(DecimalUtils.ZERO);
        BigDecimal result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(DecimalUtils.ZERO, result);

        rec.setAvailable(BigDecimal.valueOf(45L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO).setRefundCost(DecimalUtils.ZERO);
        BigDecimal expect = BigDecimal.valueOf(60L).setScale(DecimalUtils.SCALE_2);  // 100 - 40
        result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(expect, result);

        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2).setScale(DecimalUtils.SCALE_2))
                .setAvailable(BigDecimal.valueOf(55L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO).setRefundCost(DecimalUtils.ZERO);
        expect = BigDecimal.valueOf(22.5).setScale(DecimalUtils.SCALE_2);  // 45 / 200 * 100
        result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(expect, result);
    }


    @Test
    public void test_accountCost4Unfreeze() {
        PointRecPo rec = new PointRecPo();
        BigDecimal delta = BigDecimal.valueOf(45).setScale(DecimalUtils.SCALE_2);

        rec.setTotalCost(DecimalUtils.ZERO);
        BigDecimal result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(DecimalUtils.ZERO, result);

        rec.setFrozen(BigDecimal.valueOf(45L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(30L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(BigDecimal.valueOf(70L).setScale(DecimalUtils.SCALE_2))
                .setRefundCost(DecimalUtils.ZERO);
        BigDecimal expect = BigDecimal.valueOf(70L).setScale(DecimalUtils.SCALE_2);  //
        result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(expect, result);

        rec.setTotal(BigDecimal.valueOf(200L).setScale(DecimalUtils.SCALE_2))
                .setFrozen(BigDecimal.valueOf(55L).setScale(DecimalUtils.SCALE_2))
                .setTotalCost(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setUsedCost(BigDecimal.valueOf(40L).setScale(DecimalUtils.SCALE_2))
                .setFrozenCost(DecimalUtils.ZERO).setRefundCost(DecimalUtils.ZERO);
        expect = BigDecimal.valueOf(22.5).setScale(DecimalUtils.SCALE_2);  // 45 / 200 * 100
        result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(expect, result);
    }
}
