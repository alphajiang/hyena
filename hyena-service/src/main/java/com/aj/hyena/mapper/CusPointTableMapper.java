package com.aj.hyena.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CusPointTableMapper {
    List<String> listCusPointTables(@Param(value="prefix") String prefix);

    void createCusPointTable(@Param(value="tableName") String tableName);
}
