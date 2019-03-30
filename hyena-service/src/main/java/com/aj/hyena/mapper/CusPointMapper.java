package com.aj.hyena.mapper;

import com.aj.hyena.model.po.CusPointPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CusPointMapper {
    Integer addPoint(@Param(value="tableName") String tableName,
                     @Param(value="cusId") String cusId,
                     @Param(value="point") long point);

    CusPointPo getCusPoint(@Param(value="tableName") String tableName,
                           @Param(value="cusId") String cusId,
                           @Param(value="lock") boolean lock);

    void updateCusPoint(@Param(value="tableName") String tableName,
                        @Param(value="cusPoint") CusPointPo cusPoint);
}
