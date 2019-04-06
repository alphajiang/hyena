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

package com.aj.hyena.ds;

import com.aj.hyena.HyenaTestBase;
import com.aj.hyena.ds.service.PointService;
import com.aj.hyena.model.param.ListPointParam;
import com.aj.hyena.model.param.SortParam;
import com.aj.hyena.model.po.PointPo;
import com.aj.hyena.model.type.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestPointService extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointService.class);

    @Autowired
    private PointService pointService;

    @Before
    public void init() {
        super.init();

        this.pointService.addPoint(super.getPointType(), "gewgewgew", 998876L);
    }


    @Test
    public void test_listPoint() {
        ListPointParam param = new ListPointParam();
        param.setEnable(true).setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)))
                .setStart(0L).setSize(5);
        param.setType(super.getPointType());
        List<PointPo> list = this.pointService.listPoint(param);
        logger.info("list = {}", list);
        Assert.assertFalse(list.isEmpty());
    }

}
