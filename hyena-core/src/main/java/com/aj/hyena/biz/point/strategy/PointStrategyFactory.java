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

package com.aj.hyena.biz.point.strategy;

import com.aj.hyena.model.type.CalcType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PointStrategyFactory {

    private static PointStrategyFactory instance;
    private Map<CalcType, PointStrategy> strategyMap = new HashMap<>();

    public static void addStrategy(PointStrategy strategy) {
        PointStrategyFactory fact = PointStrategyFactory.getInstance();
        fact.strategyMap.put(strategy.getType(), strategy);
    }

    public static Optional<PointStrategy> getStrategy(CalcType type) {
        return Optional.ofNullable(PointStrategyFactory.getInstance().strategyMap.get(type));
    }


    private static synchronized PointStrategyFactory getInstance() {
        if (instance == null) {
            instance = new PointStrategyFactory();
        }
        return instance;
    }
}
