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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.ds.mapper.PointTableMapper;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Service
public class PointTableDs {
    private static final Logger logger = LoggerFactory.getLogger(PointTableDs.class);

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriver;

    @Autowired
    private PointTableMapper pointTableMapper;

    private Set<String> tables = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void init() {
        logger.info(">>");
        this.refreshTables();
        logger.info("<<");
    }

    public List<String> listTable() {
        return List.copyOf(this.tables);
    }

    public boolean isTableExists(String tableName) {
        return this.tables.contains(tableName);
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
        this.pointTableMapper.createPointTable(pointTableName);
        this.pointTableMapper.createPointLogTable(pointTableName);

        this.pointTableMapper.createPointRecTable(pointTableName);


        this.pointTableMapper.createPointRecordLogTable(pointTableName);

        this.pointTableMapper.createFreezeOrderRecTable(pointTableName);

        if(StringUtils.equals(datasourceDriver, HyenaConstants.CONST_TEST_DB_DRIVER)) {
            // 单元测试使用h2
            this.pointTableMapper.createPointTableIndexH2(pointTableName);
        }
        else {
            this.pointTableMapper.createPointTableIndex(pointTableName);
            this.pointTableMapper.createPointLogTableIndexPid(pointTableName);
            this.pointTableMapper.createPointLogTableIndexUid(pointTableName);
            this.pointTableMapper.createPointLogTableIndexOrderNo(pointTableName);
            this.pointTableMapper.createPointRecTableIndexPid(pointTableName);
            this.pointTableMapper.createPointRecTableIndexTag(pointTableName);
            this.pointTableMapper.createPointRecTableIndexOrderNo(pointTableName);
            this.pointTableMapper.createPointRecordLogTableIndexPid(pointTableName);
            this.pointTableMapper.createPointRecordLogTableIndexRecId(pointTableName);
            this.pointTableMapper.createFreezeOrderRecTableIndex(pointTableName);
        }
        //}
        this.refreshTables();
    }


    private synchronized void refreshTables() {
        List<String> tableList = this.pointTableMapper.listCusPointTables(HyenaConstants.PREFIX_POINT_TABLE_NAME);

        logger.info("a tableList = {}", tableList);

        tableList = tableList.stream().map(name -> name.toLowerCase())
                .filter(name -> name.startsWith(HyenaConstants.PREFIX_POINT_TABLE_NAME)
                        && !(name.endsWith("_log") || name.endsWith("_rec")))
                .collect(Collectors.toList());
        logger.info("tableList = {}", tableList);
        tables.clear();
        tables.addAll(tableList);
    }

}
