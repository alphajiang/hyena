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
            executeSql(t-> upgradeSchemaMapper.addPointRefund((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointLogRefund((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecRefund((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogRefund((String)t), p);

        });
    }

    public void addCostColumns(List<String> pointTypes) {
        pointTypes.stream().forEach(p-> {
            executeSql(t-> upgradeSchemaMapper.addPointCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointFrozenCost((String)t), p);

            executeSql(t-> upgradeSchemaMapper.addPointLogDeltaCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointLogFrozenCost((String)t), p);

            executeSql(t-> upgradeSchemaMapper.addPointRecFrozenCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecRefundCost((String)t), p);

            executeSql(t-> upgradeSchemaMapper.addPointRecLogDeltaCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogFrozenCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogUsedCost((String)t), p);
            executeSql(t-> upgradeSchemaMapper.addPointRecLogRefundCost((String)t), p);
        });
    }


    private void executeSql(Consumer f, String pointType) {
        try {
            f.accept(pointType);
        }catch (Exception e) {
            log.error("{} failed. error = {}",f.toString(),  e.getMessage(), e);
        }
    }
}
