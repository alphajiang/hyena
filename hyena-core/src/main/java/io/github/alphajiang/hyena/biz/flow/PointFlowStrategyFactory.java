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

package io.github.alphajiang.hyena.biz.flow;

import io.github.alphajiang.hyena.model.type.CalcType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class PointFlowStrategyFactory {

    //private static PointFlowStrategyFactory instance;
    private Map<CalcType, PointFlowStrategy> strategyMap = new HashMap<>();

    public void addStrategy(PointFlowStrategy strategy) {
        //PointFlowStrategyFactory fact = PointFlowStrategyFactory.getInstance();
        strategyMap.put(strategy.getType(), strategy);
    }

    public  Optional<PointFlowStrategy> getStrategy(CalcType type) {
        return Optional.ofNullable(strategyMap.get(type));
    }

@Transactional
    public void addFlow(PointFlowWrapper o) {
        Optional<PointFlowStrategy> strategy = getStrategy(o.getCalcType());
        strategy.ifPresent(act -> act.addFlow(o.getUsage(), o.getPoint()));
    }
//    private static synchronized PointFlowStrategyFactory getInstance() {
//        if (instance == null) {
//            instance = new PointFlowStrategyFactory();
//        }
//        return instance;
//    }
}
