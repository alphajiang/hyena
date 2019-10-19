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
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaTestAssert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestPointDs extends HyenaTestBase {

    @Autowired
    private PointDs pointDs;

    @BeforeEach
    public void init() {
        super.init();
        PointPo point = new PointPo();
        point.setUid("gewgewgew").setName("ut tom").setPoint(998876L);
        this.pointDs.addPoint(super.getPointType(), point);
    }

    @Test
    public void test_batchUpdate() {
        List<PointPo> list = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            PointPo p = new PointPo();
            p.setSeqNum(i + 1).setId(i + 1);
            if (i % 2 == 0) {
                p.setName("bwlegjaalgejg")
                        .setPoint(123L)
                        .setAvailable(456L)
                        .setUsed(3423L)
                        .setFrozen(3234L)
                        .setRefund(323L)
                        .setExpire(111L)
                        .setCost(222L)
                        .setFrozenCost(333L)
                        .setEnable(false);
            }
            list.add(p);
        }
        this.pointDs.batchUpdate(super.getPointType(), list);
    }

    @Test
    public void test_listPoint() {
        ListPointParam param = new ListPointParam();
        param.setEnable(true).setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)))
                .setStart(0L).setSize(5);
        param.setType(super.getPointType());
        List<PointPo> list = this.pointDs.listPoint(param);
        log.info("list = {}", list);
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    public void test_listPoint4Page() {
        ListPointParam param = new ListPointParam();
        param.setEnable(null);
        param.setType(super.getPointType());
        ListResponse<PointPo> ret = this.pointDs.listPoint4Page(param);
        log.info("ret = {}", ret);
        Assertions.assertTrue(ret.getTotal() > 0L);
        Assertions.assertFalse(ret.getData().isEmpty());
    }

    @Test
    public void test_getPointVo() {
        var result = this.pointDs.getPointVo(super.getPointType(), null, super.getUid());
        log.info("result = {}", result);
        Assertions.assertNotNull(result);

        result = this.pointDs.getPointVo(super.getPointType(), super.getUserPoint().getId(), null);
        log.info("result = {}", result);
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getPointVo_no_pid_or_uid() {
        Assertions.assertThrows(HyenaParameterException.class, () -> {
            this.pointDs.getPointVo(super.getPointType(), null, null);
            Assertions.fail();
        });
    }


    @Test
    public void test_update() {
        PointPo point = this.pointDs.getCusPoint(super.getPointType(), super.getUid(), true);
        point.setSeqNum(point.getSeqNum() + 1).setName("测试改个名字")
                .setPoint(111L).setAvailable(222L).setUsed(333L)
                .setFrozen(444L).setRefund(555L).setExpire(666L)
                .setCost(777L).setFrozenCost(888L).setEnable(false);
        this.pointDs.update(super.getPointType(), point);

        PointPo result = this.pointDs.getCusPoint(super.getPointType(), super.getUid(), false);
        HyenaTestAssert.assertEquals(point, result);
    }


    @Test
    public void test_disableAccount() {
        this.pointDs.disableAccount(super.getPointType(), super.getUid());

        PointPo result = this.pointDs.getCusPoint(super.getPointType(), super.getUid(), false);
        Assertions.assertFalse(result.getEnable());
    }
}
