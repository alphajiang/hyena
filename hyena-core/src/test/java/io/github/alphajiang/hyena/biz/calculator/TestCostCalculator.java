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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCostCalculator {


    private CostCalculator calculator;

    @BeforeEach
    public void setup() {
        calculator = new CostCalculator();
    }

    @Test
    public void test_getAvailableCost() {
        PointRecPo rec = new PointRecPo();
        rec.setTotalCost(0L);
        long result = this.calculator.getAvailableCost(rec);
        Assertions.assertEquals(0L, result);

        rec.setTotalCost(100L).setFrozenCost(5L).setUsedCost(6L)
                .setRefundCost(7L);
        long expect = 100L - 5L - 6L - 7L;
        result = this.calculator.getAvailableCost(rec);
        Assertions.assertEquals(expect, result);
    }

    @Test
    public void test_accountPoint() {
        PointRecPo rec = new PointRecPo();
        long cost = 45;
        rec.setAvailable(0L);
        long result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(0L, result);

        rec.setTotalCost(100L).setUsedCost(80L).setFrozenCost(0L).setRefundCost(0L);
        result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(0L, result);

        rec.setTotal(200L).setTotalCost(100L).setUsedCost(50L)
                .setFrozenCost(0L).setRefundCost(0L);
        long expect = 90L;  //  45 / 100 * 200
        result = this.calculator.accountPoint(rec, cost);
        Assertions.assertEquals(expect, result);
    }


    @Test
    public void test_accountCost() {
        PointRecPo rec = new PointRecPo();
        long delta = 45;

        rec.setTotalCost(0L);
        long result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(0L, result);

        rec.setAvailable(45L).setTotalCost(100L).setUsedCost(40L).setFrozenCost(0L).setRefundCost(0L);
        long expect = 60L;  // 100 - 40
        result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(expect, result);

        rec.setTotal(200L).setAvailable(55L)
                .setTotalCost(100L).setUsedCost(40L)
                .setFrozenCost(0L).setRefundCost(0L);
        expect = 22L;  // 45 / 200 * 100
        result = this.calculator.accountCost(rec, delta);
        Assertions.assertEquals(expect, result);
    }


    @Test
    public void test_accountCost4Unfreeze() {
        PointRecPo rec = new PointRecPo();
        long delta = 45;

        rec.setTotalCost(0L);
        long result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(0L, result);

        rec.setFrozen(45L).setTotalCost(100L).setUsedCost(30L).setFrozenCost(70L).setRefundCost(0L);
        long expect = 70L;  //
        result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(expect, result);

        rec.setTotal(200L).setFrozen(55L)
                .setTotalCost(100L).setUsedCost(40L)
                .setFrozenCost(0L).setRefundCost(0L);
        expect = 22L;  // 45 / 200 * 100
        result = this.calculator.accountCost4Unfreeze(rec, delta);
        Assertions.assertEquals(expect, result);
    }
}
