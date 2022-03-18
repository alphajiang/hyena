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

package io.github.alphajiang.hyena.ds.service;

import io.github.alphajiang.hyena.ds.mapper.FreezeOrderRecMapper;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FreezeOrderRecDs {

    @Autowired
    private FreezeOrderRecMapper freezeOrderRecMapper;

    @Autowired
    private PointTableDs pointTableDs;

    public void batchInsert(@NonNull String type, List<FreezeOrderRecPo> freezeOrderRecList) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.freezeOrderRecMapper.batchInsert(pointTableName, freezeOrderRecList);
    }

    public void closeByIdList(@NonNull String type, List<String> idList) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.freezeOrderRecMapper.closeByIdList(pointTableName, idList);
    }


    public List<FreezeOrderRecPo> getFreezeOrderRecList(String type,
                                                        long pid,
                                                        Integer orderType,
                                                        String orderNo) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.freezeOrderRecMapper.getFreezeOrderRecList(pointTableName, pid, orderType, orderNo);
    }


}
