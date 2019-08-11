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

package io.github.alphajiang.hyena.ds;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestPointDs extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointDs.class);

    @Autowired
    private PointDs pointDs;

    @Before
    public void init() {
        super.init();
        PointPo point = new PointPo();
        point.setUid("gewgewgew").setName("ut tom").setPoint(998876L);
        this.pointDs.addPoint(super.getPointType(), point);
    }


    @Test
    public void test_listPoint() {
        ListPointParam param = new ListPointParam();
        param.setEnable(true).setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)))
                .setStart(0L).setSize(5);
        param.setType(super.getPointType());
        List<PointPo> list = this.pointDs.listPoint(param);
        logger.info("list = {}", list);
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void test_listPoint4Page() {
        ListPointParam param = new ListPointParam();
        param.setEnable(null);
        param.setType(super.getPointType());
        ListResponse<PointPo> ret = this.pointDs.listPoint4Page(param);
        logger.info("ret = {}", ret);
        Assert.assertTrue(ret.getTotal() > 0L);
        Assert.assertFalse(ret.getData().isEmpty());
    }
}
