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

@Slf4j
@Repository
public class UpgradeSchemaDs {



    @Autowired
    private UpgradeSchemaMapper upgradeSchemaMapper;


    public void addRefundColumn(List<String> pointTypes) {
        pointTypes.stream().forEach(p -> addRefundColumn(p));
    }

    private void addRefundColumn(String pointType) {
        try {
            upgradeSchemaMapper.addPointRefund(pointType);
        }catch (Exception e) {
            log.error("addPointRefund failed. error = {}", e.getMessage(), e);
        }
        try {
            upgradeSchemaMapper.addPointLogRefund(pointType);
        }catch (Exception e) {
            log.error("addPointLogRefund failed. error = {}", e.getMessage(), e);
        }
        try {
            upgradeSchemaMapper.addPointRecRefund(pointType);
        }catch (Exception e) {
            log.error("addPointRecRefund failed. error = {}", e.getMessage(), e);
        }
        try {
            upgradeSchemaMapper.addPointRecLogRefund(pointType);
        }catch (Exception e) {
            log.error("addPointRecLogRefund failed. error = {}", e.getMessage(), e);
        }
    }
}
