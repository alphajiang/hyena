package com.aj.hyena.mapper;

import com.aj.hyena.model.po.PointPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointMapper {
    Integer addPoint(@Param(value="tableName") String tableName,
                     @Param(value="cusId") String cusId,
                     @Param(value="point") long point);

    PointPo getCusPoint(@Param(value="tableName") String tableName,
                        @Param(value="cusId") String cusId,
                        @Param(value="lock") boolean lock);

    void updateCusPoint(@Param(value="tableName") String tableName,
                        @Param(value="cusPoint") PointPo cusPoint);
}
