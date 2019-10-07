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
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestPointLogDs extends HyenaTestBase {

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointBuilder pointBuilder;

    @Before
    public void init() {
        super.init();
    }

    @Test
    public void test_addPointLog() {
        PointLogPo pl = this.buildPointLog(45L);
        this.pointLogDs.addPointLog(super.getPointType(), pl);
    }

    @Test
    public void test_listPointLog4Page() {
        ListPointLogParam param = new ListPointLogParam();
        param.setUid(super.getUid()).setOrderNo("abcd123").setSk("abewfgewgewgewgewg")
                .setType(super.getPointType());
        ListResponse<PointLog> res = this.pointLogDs.listPointLog4Page(param);
        Assert.assertNotNull(res);
    }

    @Test
    public void test_batchInsert() {
        long seq = 1L;
        List<PointLogPo> list = List.of(
                this.buildPointLog(seq++),
                this.buildPointLog(seq++)
        );
        this.pointLogDs.batchInsert(super.getPointType(), list);
    }

    private PointLogPo buildPointLog(long seq) {
        PointUsage usage = new PointUsage();
        usage.setPoint(1000L);
        PointPo point = super.getUserPoint();
        point.setSeqNum(seq);
        return this.pointBuilder.buildPointLog(PointOpType.INCREASE, usage, point);
    }
}
