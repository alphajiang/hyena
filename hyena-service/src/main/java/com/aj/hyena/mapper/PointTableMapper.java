package com.aj.hyena.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointTableMapper {
    List<String> listCusPointTables(@Param(value = "prefix") String prefix);

    void createPointTable(@Param(value = "pointTableName") String pointTableName);

    void createPointRecTable(@Param(value = "pointTableName") String pointTableName,
                             @Param(value = "pointRecTableName") String pointRecTableName);

    void createPointLogTable(@Param(value = "pointTableName") String pointTableName,
                             @Param(value = "pointRecTableName") String pointRecTableName,
                             @Param(value = "pointLogTableName") String pointLogTableName);
}
