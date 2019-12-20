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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostCalculator {

    /**
     * 计算可用成本
     *
     * @param rec 积分块
     * @return 总成本 - 已用成本 - 冻结成本
     */
    public BigDecimal getAvailableCost(PointRecPo rec) {
        if (rec.getTotalCost() == null
                || DecimalUtils.lte(rec.getTotalCost(), DecimalUtils.ZERO)) {
            return DecimalUtils.ZERO;
        }
        BigDecimal ret = rec.getTotalCost().subtract(rec.getUsedCost())
                .subtract(rec.getFrozenCost())
                .subtract(rec.getRefundCost());
        if (DecimalUtils.gt(ret, DecimalUtils.ZERO)) {
            return ret;
        } else {
            return DecimalUtils.ZERO;
        }
    }

    /**
     * 根据成本计算可用积分
     *
     * @param rec  积分块
     * @param cost 变动部分成本
     * @return 对应的积分
     */
    public BigDecimal accountPoint(PointRecPo rec, BigDecimal cost) {
        BigDecimal point = DecimalUtils.ZERO;
        BigDecimal availableCost = this.getAvailableCost(rec);
        if (DecimalUtils.lte(availableCost, DecimalUtils.ZERO)) {
            point = DecimalUtils.ZERO;
        } else if (DecimalUtils.lte(availableCost, cost)) {
            point = rec.getAvailable();
        } else {
            point = cost.multiply(rec.getTotal()).divide(rec.getTotalCost())
                    .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP);
        }
        return point;
    }

    /**
     * 计算delta(变动)部分在积分块中的所占成本
     *
     * @param rec   积分块
     * @param delta 变动部分
     * @return 所占成本
     */
    public BigDecimal accountCost(PointRecPo rec, BigDecimal delta) {
        BigDecimal cost = DecimalUtils.ZERO;
        if (rec.getTotalCost() == null
                || DecimalUtils.lte(rec.getTotalCost(), DecimalUtils.ZERO)) { // 积分块没有成本时直接返回0
            return cost;
        } else if (DecimalUtils.lte(rec.getAvailable(), delta)) { // 不够抵扣时返回剩余的全部
            cost = this.getAvailableCost(rec);
        } else { // 按比例计算
            cost = delta.multiply(rec.getTotalCost()).divide(rec.getTotal())
                    .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP);
        }
        return cost;
    }

    public BigDecimal accountCost4Unfreeze(PointRecPo rec, BigDecimal delta) {
        BigDecimal cost = DecimalUtils.ZERO;
        if (rec.getTotalCost() == null
                || DecimalUtils.lte(rec.getTotalCost(), DecimalUtils.ZERO)) { // 积分块没有成本时直接返回0
            return cost;
        } else if (DecimalUtils.lte(rec.getFrozen(), delta)) { // 不够解冻时返回剩余的全部
            cost = rec.getFrozenCost();
        } else { // 按比例计算
            cost = delta.multiply(rec.getTotalCost()).divide(rec.getTotal())
                    .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP);
        }
        return cost;
    }
}
