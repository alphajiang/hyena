/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.alphajiang.hyena.ds.mapper;

import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface PointRecMapper {

    PointRecPo getById(@Param(value = "pointTableName") String pointTableName,
                       @Param(value = "id") long id,
                       @Param(value = "lock") boolean lock);

    void addPointRec(@Param(value = "tableName") String tableName,
                     @Param(value = "rec") PointRecPo rec);

    void batchInsert(@Param(value = "pointTableName") String tableName,
                     @Param(value = "recList") List<PointRecPo> recList);

    List<PointRecDto> listPointRec(@Param(value = "pointTableName") String pointTableName,
                                   @Param(value = "param") ListPointRecParam param);

    Long countPointRec(@Param(value = "pointTableName") String pointTableName,
                       @Param(value = "param") ListPointRecParam param);

    void updatePointRec(@Param(value = "pointTableName") String pointTableName,
                        @Param(value = "rec") PointRecPo rec);


    BigDecimal getIncreasedPoint(@Param(value = "pointTableName") String pointTableName,
                                 @Param(value = "uid") String uid,
                                 @Param(value = "start") Date start,
                                 @Param(value = "end") Date end);

    void batchUpdate(@Param(value = "pointTableName") String pointTableName,
                         @Param(value = "list") List<PointRecPo> list);
}
