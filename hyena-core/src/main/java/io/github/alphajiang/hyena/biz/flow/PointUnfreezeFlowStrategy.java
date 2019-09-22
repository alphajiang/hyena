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
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PointUnfreezeFlowStrategy extends AbstractPointFlowStrategy {

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;


    @Override
    public CalcType getType() {
        return CalcType.UNFREEZE;
    }

    @Override
    @Transactional
    public void addFlow(PointUsage usage, PointPo point) {

//        PointLogPo pointLog = this.pointLogDs.addPointLog(usage.getType(), PointOpType.UNFREEZE, usage, point);
//
//        long gap = usage.getPoint();
//        List<PointRecLogPo> recLogs = new ArrayList<>();
//        try {
//            do {
//                var recLogsRet = this.unfreezePointLoop(usage.getType(), point, pointLog, gap);
//                gap = gap - recLogsRet.stream().mapToLong(PointRecLogPo::getDelta).sum();
//                recLogs.addAll(recLogsRet);
//                log.debug("gap = {}", gap);
//            } while (gap > 0L);
//        } catch (HyenaNoPointException e) {
//
//        }
//        if(gap != 0L) {
//            log.warn("no enough frozen point. usage = {}, point = {}", usage, point);
//        }
//        if (CollectionUtils.isNotEmpty(recLogs)) {
//            this.pointRecLogDs.addPointRecLogs(usage.getType(), recLogs);
//        }

    }


//    private List<PointRecLogPo> unfreezePointLoop(String type, PointPo point, PointLogPo pointLog, long expected) {
//        log.info("unfreeze. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
//        ListPointRecParam param = new ListPointRecParam();
//        param.setUid(point.getUid()).setFrozen(true).setLock(true)
//                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
//                .setSize(5);
//        var recList = this.pointRecDs.listPointRec(type, param);
//        if (recList.isEmpty()) {
//            throw new HyenaNoPointException("no enough point", Level.DEBUG);
//        }
//        long sum = 0L;
//        List<PointRecLogPo> recLogs = new ArrayList<>();
//        for (PointRecPo rec : recList) {
//            long gap = expected - sum;
//            if (gap < 1L) {
//                log.warn("gap = {} !!!", gap);
//                break;
//            } else if (rec.getFrozen() < gap) {
//                sum += rec.getFrozen();
//                long delta = rec.getFrozen();
//                var retRec = this.pointRecDs.unfreezePoint(type, rec, gap);
//                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta);
//                recLogs.add(recLog);
//            } else {
//                sum += gap;
//                var retRec = this.pointRecDs.unfreezePoint(type, rec, gap);
//                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, gap);
//                recLogs.add(recLog);
//                break;
//            }
//        }
//        var ret = expected - sum;
//        log.debug("ret = {}", ret);
//        return recLogs;
//    }
}
