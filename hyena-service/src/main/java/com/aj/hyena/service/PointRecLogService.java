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
