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

import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointMapper {
    Integer addPoint(@Param(value = "tableName") String tableName,
                     @Param(value = "point") PointPo point);


    PointPo getCusPoint(@Param(value = "tableName") String tableName,
                             @Param(value = "id") long id,
                             @Param(value = "lock") boolean lock);

    PointPo getCusPointByUid(@Param(value = "tableName") String tableName,
                        @Param(value = "uid") String uid,
                        @Param(value = "lock") boolean lock);


    List<PointPo> listPoint(@Param(value = "pointTableName") String pointTableName,
                            @Param(value = "param") ListPointParam param);


    long countPoint(@Param(value = "pointTableName") String pointTableName,
                    @Param(value = "param") ListPointParam param);

    void updateCusPoint(@Param(value = "tableName") String tableName,
                        @Param(value = "cusPoint") PointPo cusPoint);
}
