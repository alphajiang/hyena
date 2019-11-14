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

package io.github.alphajiang.hyena.biz.strategy;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class TestPointCancelStrategy extends TestPointStrategyBase {


    @Autowired
    private PointStrategy pointCancelStrategy;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Test
    public void test_cancelPoint_byRecId() throws InterruptedException {
        log.info(">> test start");
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(super.uid).setStart(0L).setSize(1);
        List<PointRecDto> recList = this.pointRecDs.listPointRec(super.getPointType(), param);
        PointRecDto rec = recList.get(0);
        log.info("rec = {}", rec);
        long number = rec.getAvailable();
        long resultAvailable = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setRecId(rec.getId())
                .setUid(this.uid).setPoint(number).setNote("test_cancelPoint_byRecId");
        PointPo result = this.pointCancelStrategy.process(usage);
        log.info("result = {}", result);
        // Assertions.assertEquals(number, result.getPoint().longValue());
        Assertions.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(0L, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(100);
        PointRecPo resultRec = this.pointRecDs.getById(super.getPointType(), rec.getId(), false);
        log.info("resultRec = {}", resultRec);
        Assertions.assertFalse(resultRec.getEnable());
        Assertions.assertTrue(resultRec.getAvailable().longValue() == 0L);
        Assertions.assertTrue(resultRec.getCancelled().longValue() == number);
        log.info("<< test end");
    }

    @Test
    public void test_cancelPoint_nonRecId() throws InterruptedException {
        log.info(">> test start");
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(super.uid).setStart(0L).setSize(1);
        List<PointRecDto> recList = this.pointRecDs.listPointRec(super.getPointType(), param);
        PointRecDto rec = recList.get(0);

        long number = rec.getAvailable();
        long resultAvailable = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType())
                .setUid(this.uid).setPoint(number).setNote("test_cancelPoint");
        PointPo result = this.pointCancelStrategy.process(usage);
        log.info("result = {}", result);
        Assertions.assertEquals(resultAvailable, result.getPoint().longValue());
        Assertions.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(0L, result.getFrozen().longValue());
        Assertions.assertEquals(0L, result.getExpire().longValue());

        Thread.sleep(100L);
        PointRecPo resultRec = this.pointRecDs.getById(super.getPointType(), rec.getId(), false);
        log.info("resultRec = {}", resultRec);
        //Assertions.assertFalse(resultRec.getEnable());
        Assertions.assertTrue(resultRec.getAvailable().longValue() == 0L);
        Assertions.assertTrue(resultRec.getCancelled().longValue() == number);

        ListPointLogParam listPointLogParam = new ListPointLogParam();
        listPointLogParam.setPid(result.getId()).setSeqNum(result.getSeqNum()).setType(super.getPointType());
        List<PointLogDto> pointLogs = this.pointLogDs.listPointLog(listPointLogParam);
        Assertions.assertTrue(pointLogs.size() == 1);
        PointLogDto pointLog = pointLogs.get(0);
        log.info("pointLog = {}", pointLog);
        Assertions.assertEquals(number, pointLog.getDelta().longValue());
        Assertions.assertEquals(PointOpType.CANCEL.code(), pointLog.getType().intValue());
        Assertions.assertEquals(0L, pointLog.getPoint().longValue());
        Assertions.assertEquals(0L, pointLog.getAvailable().longValue());
        Assertions.assertEquals(0L, pointLog.getUsed().longValue());
        Assertions.assertEquals(0L, pointLog.getFrozen().longValue());
        log.info("<< test end");
    }
}
