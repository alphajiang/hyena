package com.aj.hyena.mapper;

import com.aj.hyena.model.po.PointRecLogPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointRecLogMapper {
    void addPointRecLog(@Param(value = "tableName") String tableName,
                        @Param(value = "recLog") PointRecLogPo recLog);
}
