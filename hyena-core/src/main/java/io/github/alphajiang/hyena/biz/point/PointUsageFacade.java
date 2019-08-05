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

import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategyFactory;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PointUsageFacade {

    /**
     * 增加积分
     *
     * @param usage 增加积分参数
     * @return 增加后的用户积分
     */
    @Transactional
    public PointPo increase(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.INCREASE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 减少(使用)积分
     *
     * @param usage 减少积分参数
     * @return 减少后的用户积分
     */
    @Transactional
    public PointPo decrease(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.DECREASE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 减少(使用)已冻结的积分
     *
     * @param usage 减少积分参数
     * @return 减少后的用户积分
     */
    @Transactional
    public PointPo decreaseFrozen(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.DECREASE_FROZEN);
        if(usage.getUnfreezePoint() != null && usage.getUnfreezePoint() > 0L){
            // 有需要解冻的积分, 先做解冻操作
            Optional<PointStrategy> unfreezeStrategy = PointStrategyFactory.getStrategy(CalcType.UNFREEZE);
            PointUsage usage4Unfreeze = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Unfreeze);
            usage4Unfreeze.setPoint(usage.getUnfreezePoint());
            unfreezeStrategy.ifPresent(act -> act.process(usage4Unfreeze));
        }
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }


    /**
     * 冻结积分
     *
     * @param usage 冻结参数
     * @return 冻结后的用户积分
     */
    @Transactional
    public PointPo freeze(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.FREEZE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 解冻积分
     *
     * @param usage 解冻参数
     * @return 解冻后的用户积分
     */
    @Transactional
    public PointPo unfreeze(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.UNFREEZE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    @Transactional
    public PointPo cancel(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.CANCEL);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    @Transactional
    public PointPo expire(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.EXPIRE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }
}
