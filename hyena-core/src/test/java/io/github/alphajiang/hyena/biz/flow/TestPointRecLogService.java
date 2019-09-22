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

package io.github.alphajiang.hyena.biz.flow;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TestPointRecLogService extends HyenaTestBase {

    @Autowired
    private PointRecLogService pointRecLogService;

    @Before
    public void init() {
        super.init();

    }

    @Test
    public void test_addRecLog() throws InterruptedException {
        log.info(">>");
        PointRecPo rec = new PointRecPo();
        rec.setPid(super.getUserPoint().getId())
                .setAvailable(1000L).setCancelled(0L)
                .setExpire(1L).setFrozen(20L).setTotal(1000L).setUsed(100L)
        .setId(100L);
        this.pointRecLogService.addLogByRec(super.getPointType(), PointOpType.INCREASE,
                rec, 100L, 100L, "UT-test_addRecLog");

        Thread.sleep(1000L);
        log.info("<<");
        }
}
