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

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PointFreezeFlowStrategy extends AbstractPointFlowStrategy {

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;


    @Override
    public CalcType getType() {
        return CalcType.FREEZE;
    }

    @Override
    @Transactional
    public void addFlow(PointUsage usage, PointPo point) {
        //PointLogPo pointLog = this.pointLogDs.addPointLog(usage.getType(), PointOpType.FREEZE, usage, point);
        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.FREEZE, usage, point);

        long gap = usage.getPoint();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.freezePointLoop(usage.getType(), point, pointLog, gap);
                gap = gap - recLogsRet.stream().mapToLong(PointRecLogPo::getDelta).sum();
                recLogs.addAll(recLogsRet);
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if (gap != 0L) {
            log.warn("no enough available point! gap = {}", gap);
            //throw new HyenaServiceException("no enough available point!");
        }
        this.pointLogDs.addPointLog(usage.getType(), pointLog);
        if(CollectionUtils.isNotEmpty(recLogs)) {
            this.pointRecLogDs.addPointRecLogs(usage.getType(), recLogs);
        }
    }


    private List<PointRecLogPo> freezePointLoop(String type, PointPo point, PointLogPo pointLog, long expected) {
        log.info("freeze. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(point.getUid()).setAvailable(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecDs.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        long sum = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                long deltaCost = this.pointRecDs.accountCost(rec, delta);
                var retRec = this.pointRecDs.freezePoint(type, rec, gap, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog( retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                //sum += gap;
                long deltaCost = this.pointRecDs.accountCost(rec, gap);
                var retRec = this.pointRecDs.freezePoint(type, rec, gap, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog( retRec, pointLog, gap, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        log.debug("recLogs = {}", recLogs);
        return recLogs;
    }
}
