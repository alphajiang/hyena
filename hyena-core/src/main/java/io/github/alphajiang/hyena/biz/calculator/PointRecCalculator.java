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

import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PointRecCalculator {

    @Autowired
    private CostCalculator costCalculator;

    public PointRecCalcResult freezePoint(PointRecPo rec, BigDecimal delta) {
        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setId(rec.getId());
        BigDecimal deltaCost;
        if (DecimalUtils.lt(rec.getAvailable(), delta)) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        }
        if (DecimalUtils.gt(rec.getAvailable(), delta)) {
            BigDecimal available = rec.getAvailable().subtract(delta);
            BigDecimal frozen = rec.getFrozen().add(delta);
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available)
                    .setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost().add(deltaCost));
        } else {
            BigDecimal frozen = rec.getFrozen().add(rec.getAvailable());
            deltaCost = costCalculator.getAvailableCost(rec);
            rec.setAvailable(DecimalUtils.ZERO)
                    .setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost().add(deltaCost));
        }
        rec4Update.setAvailable(rec.getAvailable())
                .setFrozen(rec.getFrozen())
                .setFrozenCost(rec.getFrozenCost());
        return new PointRecCalcResult(rec4Update, deltaCost);
    }


    public PointRecCalcResult unfreezePoint(PointRecPo rec, BigDecimal delta, BigDecimal deltaCost) {
        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setId(rec.getId());


        if (DecimalUtils.lt(rec.getFrozen(), delta)) {
            log.warn("no enough frozen point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough frozen point", Level.WARN);
        } else if (DecimalUtils.gt(rec.getFrozen(), delta)) {
            BigDecimal frozen = rec.getFrozen().subtract(delta);
            BigDecimal available = rec.getAvailable().add(delta);
            if (deltaCost == null) {
                deltaCost = this.costCalculator.accountCost4Unfreeze(rec, delta);
            }
            rec.setAvailable(available).setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost().subtract(deltaCost));
        } else {
            BigDecimal available = rec.getAvailable().add(rec.getFrozen());
            if (deltaCost == null) {
                deltaCost = rec.getFrozenCost();
            }
            rec.setFrozen(DecimalUtils.ZERO).setAvailable(available)
                    .setFrozenCost(DecimalUtils.ZERO);
        }
        rec4Update.setFrozen(rec.getFrozen())
                .setAvailable(rec.getAvailable())
                .setFrozenCost(rec.getFrozenCost());
        return new PointRecCalcResult(rec4Update, deltaCost);
    }

    public PointRecCalcResult decreasePoint(PointRecPo rec, BigDecimal delta) {
        BigDecimal deltaCost;
        if (DecimalUtils.lt(rec.getAvailable(), delta)) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        } else if (DecimalUtils.gt(rec.getAvailable(), delta)) {
            BigDecimal available = rec.getAvailable().subtract(delta);
            BigDecimal used = rec.getUsed().add(delta);
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available).setUsed(used).setUsedCost(rec.getUsedCost().add(deltaCost));
        } else {
            BigDecimal used = rec.getUsed().add(rec.getAvailable());
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(DecimalUtils.ZERO).setUsed(used).setUsedCost(rec.getUsedCost().add(deltaCost));
            if (DecimalUtils.lte(rec.getFrozen(), DecimalUtils.ZERO)) {
                rec.setEnable(false);
            }
        }
        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setAvailable(rec.getAvailable())
                .setUsed(rec.getUsed())
                .setUsedCost(rec.getUsedCost())
                .setEnable(rec.getEnable())
                .setId(rec.getId());
        return new PointRecCalcResult(rec4Update, deltaCost);
    }


    public PointRecPo refundPoint(PointRecPo rec, BigDecimal delta, BigDecimal cost) {
        rec.setAvailable(rec.getAvailable().subtract(delta))
                .setRefund(rec.getRefund().add(delta))
                .setRefundCost(rec.getRefundCost().add(cost));
        if (DecimalUtils.lte(rec.getAvailable(), DecimalUtils.ZERO)
                && DecimalUtils.lte(rec.getFrozen(), DecimalUtils.ZERO)) {
            rec.setEnable(false);
        }

        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setAvailable(rec.getAvailable())
                .setRefund(rec.getRefund())
                .setRefundCost(rec.getRefundCost())
                .setEnable(rec.getEnable())
                .setId(rec.getId());
        return rec4Update;
    }


    public PointRecCalcResult cancelPoint(PointRecPo rec, BigDecimal delta) {
        BigDecimal deltaCost;
        if (DecimalUtils.lt(rec.getAvailable(), delta)) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        } else if (DecimalUtils.gt(rec.getAvailable(), delta)) {
            BigDecimal available = rec.getAvailable().subtract(delta);
            BigDecimal canceled = rec.getCancelled().add(delta);
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available).setCancelled(canceled)
                    .setUsedCost(rec.getUsedCost().add(deltaCost));

        } else {
            BigDecimal canceled = rec.getCancelled().add(rec.getAvailable());
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(DecimalUtils.ZERO).setCancelled(canceled)
                    .setUsedCost(rec.getUsedCost().add(deltaCost));
            if (DecimalUtils.lte(rec.getFrozen(), DecimalUtils.ZERO)) {
                rec.setEnable(false);
            }
        }

        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setAvailable(rec.getAvailable())
                .setCancelled(rec.getCancelled())
                .setUsedCost(rec.getUsedCost())
                .setEnable(rec.getEnable())
                .setId(rec.getId());
        return new PointRecCalcResult(rec4Update, deltaCost);

    }
}
