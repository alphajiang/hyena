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

package com.aj.hyena.ds.service;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.ds.mapper.PointTableMapper;
import com.aj.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Service
public class PointTableService {
    private static final Logger logger = LoggerFactory.getLogger(PointTableService.class);


    @Autowired
    private PointTableMapper cusPointTableMapper;

    private Set<String> tables = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void init() {
        logger.info(">>");
        this.listTables();
        logger.info("<<");
    }

    public String getOrCreateTable(String type) {
        String tableName = TableNameHelper.getPointTableName(type);
        if (!this.tables.contains(tableName)) {
            this.createTable(type);
        }
        return tableName;
    }

    private void createTable(String type) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.cusPointTableMapper.createPointTable(pointTableName);
        this.cusPointTableMapper.createPointRecTable(pointTableName);
        this.cusPointTableMapper.createPointRecTableIndex(pointTableName);
        this.cusPointTableMapper.createPointLogTable(pointTableName);
        this.cusPointTableMapper.createPointLogTableIndex(pointTableName);
        this.listTables();
    }

    private synchronized void listTables() {
        List<String> tableList = this.cusPointTableMapper.listCusPointTables(HyenaConstants.PREFIX_POINT_TABLE_NAME);

        logger.info("a tableList = {}", tableList);

        tableList = tableList.stream().map(name -> name.toLowerCase())
                .filter(name ->  name.startsWith(HyenaConstants.PREFIX_POINT_TABLE_NAME)
                && !(name.endsWith("_rec") || name.endsWith("_rec_log")))
                .collect(Collectors.toList());
        logger.info("tableList = {}", tableList);
        tables.clear();
        tables.addAll(tableList);
    }

}
