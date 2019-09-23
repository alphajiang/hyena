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

package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.springframework.stereotype.Service;

@Service
public class CostCalculator {

    /**
     * 计算可用成本
     * @param rec 积分块
     * @return 总成本 - 已用成本 - 冻结成本
     */
    public long getAvailableCost(PointRecPo rec) {
        if (rec.getTotalCost() == null || rec.getTotalCost() < 1L) {
            return 0L;
        }
        return rec.getTotalCost() - rec.getUsedCost() - rec.getFrozenCost()
                - rec.getRefundCost();
    }

    /**
     * 根据成本计算可用积分
     *
     * @param rec 积分块
     * @param cost 变动部分成本
     * @return 对应的积分
     */
    public long accountPoint(PointRecPo rec, long cost) {
        long point = 0L;
        long availableCost = this.getAvailableCost(rec);
        if (availableCost < 1L) {
            point = 0L;
        } else if (availableCost <= cost) {
            point = rec.getAvailable();
        } else {
            point = cost * rec.getTotal() / rec.getTotalCost();
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
    public long accountCost(PointRecPo rec, long delta) {
        long cost = 0L;
        if (rec.getTotalCost() == null || rec.getTotalCost() < 1L) { // 积分块没有成本时直接返回0
            return cost;
        }
        if (rec.getAvailable() <= delta) { // 不够抵扣时返回剩余的全部
            cost = this.getAvailableCost(rec);
        } else { // 按比例计算
            cost = delta * rec.getTotalCost() / rec.getTotal();
        }
        return cost;
    }

    public long accountCost4Unfreeze(PointRecPo rec, long delta) {
        long cost = 0L;
        if (rec.getTotalCost() == null || rec.getTotalCost() < 1L) { // 积分块没有成本时直接返回0
            return cost;
        }
        if (rec.getFrozen() <= delta) { // 不够解冻时返回剩余的全部
            cost = rec.getFrozenCost();
        } else { // 按比例计算
            cost = delta * rec.getTotalCost() / rec.getTotal();
        }
        return cost;
    }
}
