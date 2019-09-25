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

package io.github.alphajiang.hyena.ds.service;

import io.github.alphajiang.hyena.ds.mapper.UpgradeSchemaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Repository
public class UpgradeSchemaDs {



    @Autowired
    private UpgradeSchemaMapper upgradeSchemaMapper;


    public void addRefundColumn(List<String> pointTypes) {
        pointTypes.stream().forEach(p -> {
            executeSql(t-> upgradeSchemaMapper.addPointRefund(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointLogRefund(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecRefund(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogRefund(t), p);

        });
    }

    public void addCostColumns(List<String> pointTypes) {
        pointTypes.stream().forEach(p-> {
            executeSql(t-> upgradeSchemaMapper.addPointCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointFrozenCost(t), p);

            executeSql(t-> upgradeSchemaMapper.addPointLogDeltaCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointLogFrozenCost(t), p);

            executeSql(t-> upgradeSchemaMapper.addPointRecFrozenCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecRefundCost(t), p);

            executeSql(t-> upgradeSchemaMapper.addPointRecLogDeltaCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogFrozenCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogUsedCost(t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogRefundCost(t), p);
        });
    }


    private void executeSql(Consumer<String> f, String pointType) {
        try {
            f.accept(pointType);
        }catch (Exception e) {
            log.error("{} failed. error = {}",f.toString(),  e.getMessage(), e);
        }
    }
}
