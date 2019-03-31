package com.aj.hyena.mapper;

import com.aj.hyena.model.param.ListPointRecParam;
import com.aj.hyena.model.po.PointRecPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointRecMapper {


    void addPointRec(@Param(value = "tableName") String tableName,
                     @Param(value = "rec") PointRecPo rec);


    List<PointRecPo> listPointRec(@Param(value = "pointTableName") String pointTableName,
                                  @Param(value = "param") ListPointRecParam param);

    void updatePointRec(@Param(value = "pointTableName") String pointTableName,
                        @Param(value = "rec") PointRecPo rec);
}
