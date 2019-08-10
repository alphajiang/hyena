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

import io.github.alphajiang.hyena.ds.mapper.PointMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointDs {
    private static final Logger logger = LoggerFactory.getLogger(PointDs.class);

    @Autowired
    private PointMapper pointMapper;


//    @Autowired
//    private PointTableService cusPointTableService;
//
//    @Autowired
//    private PointRecService pointRecService;

    public PointPo getCusPoint(String type, String uid, boolean lock) {
        String tableName = TableNameHelper.getPointTableName(type);
        return this.pointMapper.getCusPoint(tableName, uid, lock);
    }

    public boolean addPoint(String type, String uid, String name, long point) {
        String tableName = TableNameHelper.getPointTableName(type);
        Integer ret = this.pointMapper.addPoint(tableName, uid,
                name == null ? "" : name, point);
        return ret != null && ret.intValue() > 0;
    }

    public List<PointPo> listPoint(ListPointParam param) {
        logger.debug("param = {}", param);
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        return this.pointMapper.listPoint(pointTableName, param);
    }

    public ListResponse<PointPo> listPoint4Page(ListPointParam param) {
        logger.debug("param = {}", param);
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        var list = this.pointMapper.listPoint(pointTableName, param);
        var total = this.pointMapper.countPoint(pointTableName, param);
        ListResponse<PointPo> res = new ListResponse<>(list, total);
        return res;
    }


    public void update(String type, PointPo point) {
        this.pointMapper.updateCusPoint(TableNameHelper.getPointTableName(type), point);
    }
}
