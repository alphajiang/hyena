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

package com.aj.hyena.biz.point;

import com.aj.hyena.biz.point.strategy.PointStrategy;
import com.aj.hyena.biz.point.strategy.PointStrategyFactory;
import com.aj.hyena.model.po.PointPo;
import com.aj.hyena.model.type.CalcType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PointUsageFacade {

    /**
     * 增加积分
     *
     * @param usage
     * @return
     */
    public PointPo increase(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.INCREASE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 减少(使用)积分
     *
     * @param usage
     * @return
     */
    public PointPo decrease(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.DECREASE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 减少(使用)已冻结的积分
     *
     * @param usage
     * @return
     */
    public PointPo decreaseFrozen(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.DECREASE_FROZEN);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }


    /**
     * 冻结积分
     *
     * @param usage
     * @return
     */
    public PointPo freeze(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.FREEZE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    /**
     * 解冻积分
     *
     * @param usage
     * @return
     */
    public PointPo unfreeze(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.UNFREEZE);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }

    public PointPo cancel(PointUsage usage) {
        Optional<PointStrategy> strategy = PointStrategyFactory.getStrategy(CalcType.CANCEL);
        return strategy.flatMap(act -> Optional.ofNullable(act.process(usage))).get();
    }
}
