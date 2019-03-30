package com.aj.hyena.service;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.mapper.CusPointTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class CusPointTableService {
    private static final Logger logger = LoggerFactory.getLogger(CusPointTableService.class);


    @Autowired
    private CusPointTableMapper cusPointTableMapper;

    private Set<String> tables = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void init() {
        logger.info(">>");
        this.listTables();
        logger.info("<<");
    }

    public String getOrCreateTable(String type) {
        String tableName = HyenaConstants.PREFIX_CUS_POINT_TABLE_NAME + type;
        if(!this.tables.contains(tableName)){
            this.createTable(tableName);
        }
        return tableName;
    }

    private void createTable(String tableName) {
        this.cusPointTableMapper.createCusPointTable(tableName);
        //logger.info("after create table {}", tableName);
        this.listTables();
    }

    private synchronized void listTables()  {
        List<String> tableList = this.cusPointTableMapper.listCusPointTables(HyenaConstants.PREFIX_CUS_POINT_TABLE_NAME);
        logger.info("tableList = {}", tableList);
        tables.clear();
        tables.addAll(tableList);
    }
}
