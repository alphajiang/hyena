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

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UpgradeSchemaMapper {


    void addPointRefund(@Param(value = "pointTableName") String pointTableName );
    void addPointCost(@Param(value = "pointTableName") String pointTableName );
    void addPointFrozenCost(@Param(value = "pointTableName") String pointTableName );


    void addPointLogDeltaCost(@Param(value = "pointTableName") String pointTableName );

    void addPointLogRefund(@Param(value = "pointTableName") String pointTableName );
    void addPointLogFrozenCost(@Param(value = "pointTableName") String pointTableName );

    void addPointRecRefund(@Param(value = "pointTableName") String pointTableName );
    void addPointRecFrozenCost(@Param(value = "pointTableName") String pointTableName );
    void addPointRecRefundCost(@Param(value = "pointTableName") String pointTableName );

    void addPointRecLogDeltaCost(@Param(value = "pointTableName") String pointTableName );
    void addPointRecLogRefund(@Param(value = "pointTableName") String pointTableName );
    void addPointRecLogFrozenCost(@Param(value = "pointTableName") String pointTableName );
    void addPointRecLogUsedCost(@Param(value = "pointTableName") String pointTableName );
    void addPointRecLogRefundCost(@Param(value = "pointTableName") String pointTableName );
}
