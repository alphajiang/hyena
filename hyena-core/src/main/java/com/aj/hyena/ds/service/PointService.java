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

package com.aj.hyena.ds.service;

import com.aj.hyena.ds.mapper.PointMapper;
import com.aj.hyena.model.param.ListPointParam;
import com.aj.hyena.model.po.PointPo;
import com.aj.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {
    private static final Logger logger = LoggerFactory.getLogger(PointService.class);

    @Autowired
    private PointMapper pointMapper;


//    @Autowired
//    private PointTableService cusPointTableService;
//
//    @Autowired
//    private PointRecService pointRecService;

    public PointPo getCusPoint(String type, String cusId, boolean lock) {
        String tableName = TableNameHelper.getPointTableName(type);
        var ret = this.pointMapper.getCusPoint(tableName, cusId, lock);
        return ret;
    }

    public boolean addPoint(String type, String cusId, long point) {
        String tableName = TableNameHelper.getPointTableName(type);
        Integer ret = this.pointMapper.addPoint(tableName, cusId, point);
        return ret != null && ret.intValue() > 0 ? true : false;
    }

    public List<PointPo> listPoint(ListPointParam param) {
        logger.debug("param = {}", param);
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        return this.pointMapper.listPoint(pointTableName, param);
    }


    public void update(String type, PointPo point) {
        this.pointMapper.updateCusPoint(TableNameHelper.getPointTableName(type), point);
    }
}
