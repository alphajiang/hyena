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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointRecCalculator {

    @Autowired
    private CostCalculator costCalculator;

    public PointRecCalcResult freezePoint(PointRecPo rec, long delta) {
        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setId(rec.getId());
        long deltaCost;
        if (rec.getAvailable() < delta) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        }
        if (rec.getAvailable() > delta) {
            long available = rec.getAvailable() - delta;
            long frozen = rec.getFrozen() + delta;
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available)
                    .setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost() + deltaCost);
        } else {
            long frozen = rec.getFrozen() + rec.getAvailable();
            deltaCost = costCalculator.getAvailableCost(rec);
            rec.setAvailable(0L)
                    .setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost() + deltaCost);
        }
        rec4Update.setAvailable(rec.getAvailable())
                .setFrozen(rec.getFrozen())
                .setFrozenCost(rec.getFrozenCost());
        return new PointRecCalcResult(rec4Update, deltaCost);
    }


    public PointRecCalcResult unfreezePoint(PointRecPo rec, long delta, Long deltaCost) {
        PointRecPo rec4Update = new PointRecPo();
        rec4Update.setId(rec.getId());


        if (rec.getFrozen() < delta) {
            log.warn("no enough frozen point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough frozen point", Level.WARN);
        } else if (rec.getFrozen() > delta) {
            long frozen = rec.getFrozen() - delta;
            long available = rec.getAvailable() + delta;
            if(deltaCost == null) {
                deltaCost = this.costCalculator.accountCost4Unfreeze(rec, delta);
            }
            rec.setAvailable(available).setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost() - deltaCost);
        } else {
            long available = rec.getAvailable() + rec.getFrozen();
            if(deltaCost == null) {
                deltaCost = rec.getFrozenCost();
            }
            rec.setFrozen(0L).setAvailable(available)
                    .setFrozenCost(0L);
        }
        rec4Update.setFrozen(rec.getFrozen())
                .setAvailable(rec.getAvailable())
                .setFrozenCost(rec.getFrozenCost());
        return new PointRecCalcResult(rec4Update, deltaCost);
    }

    public PointRecCalcResult decreasePoint(PointRecPo rec, long delta) {
        long deltaCost;
        if (rec.getAvailable() < delta) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        } else if (rec.getAvailable() > delta) {
            long available = rec.getAvailable() - delta;
            long used = rec.getUsed() + delta;
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available).setUsed(used).setUsedCost(rec.getUsedCost() + deltaCost);
        } else {
            long used = rec.getUsed() + rec.getAvailable();
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(0L).setUsed(used).setUsedCost(rec.getUsedCost() + deltaCost);
            if (rec.getFrozen() < 1L) {
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


    public PointRecPo refundPoint(PointRecPo rec, long delta, long cost) {
        rec.setAvailable(rec.getAvailable() - delta)
                .setRefund(rec.getRefund() + delta)
                .setRefundCost(rec.getRefundCost() + cost);
        if (rec.getAvailable() < 1L && rec.getFrozen() < 1L) {
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


    public PointRecCalcResult cancelPoint(PointRecPo rec, long delta) {
        long deltaCost;
        if (rec.getAvailable() < delta) {
            log.warn("no enough available point. rec = {}, delta = {}", rec, delta);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        } else if (rec.getAvailable() > delta) {
            long available = rec.getAvailable() - delta;
            long canceled = rec.getCancelled() + delta;
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(available).setCancelled(canceled)
                    .setUsedCost(rec.getUsedCost() + deltaCost);

        } else {
            long canceled = rec.getCancelled() + rec.getAvailable();
            deltaCost = this.costCalculator.accountCost(rec, delta);
            rec.setAvailable(0L).setCancelled(canceled)
                    .setUsedCost(rec.getUsedCost() + deltaCost);
            if (rec.getFrozen() < 1L) {
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
