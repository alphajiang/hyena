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

package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointTableService;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

abstract class AbstractPointStrategy implements PointStrategy {

    @Autowired
    private PointTableService cusPointTableService;


    @PostConstruct
    public void init() {
        PointStrategyFactory.addStrategy(this);
    }

    void preProcess(PointUsage usage) {
        //String tableName =
        HyenaAssert.notBlank(usage.getType(), "invalid parameter, 'type' can't blank");
        HyenaAssert.notBlank(usage.getUid(), "invalid parameter, 'uid' can't blank");
        HyenaAssert.isTrue(usage.getPoint() > 0L, HyenaConstants.RES_CODE_PARAMETER_ERROR, "invalid parameter, 'point' must great than 0");

        this.cusPointTableService.getOrCreateTable(usage.getType());
        //logger.debug("tableName = {}", tableName);
    }
}
