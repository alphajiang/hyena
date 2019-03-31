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

package com.aj.hyena.service;

import com.aj.hyena.mapper.PointRecLogMapper;
import com.aj.hyena.model.po.PointRecLogPo;
import com.aj.hyena.utils.TableNameHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointRecLogService {

    @Autowired
    private PointRecLogMapper pointRecLogMapper;


    public void addPointRecLog(String type, PointRecLogPo recLog) {
        String tableName = TableNameHelper.getPointRecLogTableName(type);
        this.pointRecLogMapper.addPointRecLog(tableName, recLog);
    }
}
