package com.aj.hyena.service;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.mapper.PointTableMapper;
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
        String tableName = HyenaConstants.PREFIX_POINT_TABLE_NAME + type;
        if (!this.tables.contains(tableName)) {
            this.createTable(tableName);
        }
        return tableName;
    }

    private void createTable(String tableName) {
        this.cusPointTableMapper.createPointTable(tableName);
        String recTableName = this.getRecTableName(tableName);
        this.cusPointTableMapper.createPointRecTable(tableName, this.getRecTableName(tableName));
        this.cusPointTableMapper.createPointLogTable(tableName, recTableName, this.getLogTableName(tableName));
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

    private String getRecTableName(String tableName) {
        return tableName + "_rec";
    }

    private String getLogTableName(String tableName) {
        return tableName + "_rec_log";
    }
}
