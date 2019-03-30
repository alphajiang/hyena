package com.aj.hyena.service;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.mapper.PointTableMapper;
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
        String recTableName = TableNameHelper.getPointRecTableName(type);
        String recLogTableName = TableNameHelper.getPointRecLogTableName(type);
        this.cusPointTableMapper.createPointTable(pointTableName);
        this.cusPointTableMapper.createPointRecTable(pointTableName, recTableName);
        this.cusPointTableMapper.createPointLogTable(pointTableName, recTableName, recLogTableName);
        this.listTables();
    }

    private synchronized void listTables() {
        List<String> tableList = this.cusPointTableMapper.listCusPointTables(HyenaConstants.PREFIX_POINT_TABLE_NAME);
        tableList = tableList.stream().filter(name -> !(name.endsWith("_rec") || name.endsWith("_rec_log")))
                .collect(Collectors.toList());
        logger.info("tableList = {}", tableList);
        tables.clear();
        tables.addAll(tableList);
    }

}
