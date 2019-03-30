package com.aj.hyena.mapper;

import com.aj.hyena.model.po.PointRecPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointRecMapper {

    void addPointRec(@Param(value = "tableName") String tableName,
                     @Param(value = "rec") PointRecPo rec);
}
