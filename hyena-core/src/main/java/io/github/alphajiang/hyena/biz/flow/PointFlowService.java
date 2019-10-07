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
import io.github.alphajiang.hyena.model.po.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PointFlowService {


    @Autowired
    private PointLogFlowQueue pointLogFlowQueue;

    @Autowired
    private PointRecLogFlowQueue pointRecLogFlowQueue;

    @Autowired
    private PointRecDsQueue pointRecDsQueue;

    @Autowired
    private PointUpdateQueue pointUpdateQueue;

    @Autowired
    private FreezeOrderRecDsQueue freezeOrderRecDsQueue;


    public void addFlow(PointUsage usage, PointLogPo pointLog, List<PointRecLogPo> recLogs) {
        PointLogFlowQueue.PointLog logItem = new PointLogFlowQueue.PointLog();
        logItem.setPointLog(pointLog).setType(usage.getType());
        this.pointLogFlowQueue.offer(logItem);

        recLogs.stream().forEach(o -> {
            PointRecLogFlowQueue.PointRecLog item = new PointRecLogFlowQueue.PointRecLog();
            item.setPointRecLog(o).setType(usage.getType());
            this.pointRecLogFlowQueue.offer(item);
        });

    }

    public void updatePoint(String type, PointPo point) {
        PointUpdateQueue.Point item = new PointUpdateQueue.Point();
        item.setPoint(point).setType(type);
        this.pointUpdateQueue.offer(item);
    }

//    public void insertPointRec(String type, PointRecPo rec) {
//        this.pointRecDsQueue.offer(new PointRecDsQueue.PointRec(true, type, rec));
//    }

    public void updatePointRec(String type, List<PointRecPo> recList) {
        recList.stream().forEach(o -> {
            PointRecDsQueue.PointRec item = new PointRecDsQueue.PointRec();
            item.setPointRec(o).setInsert(false).setType(type);
            this.pointRecDsQueue.offer(item);
        });
    }

    public void addFreezeOrderRec(String type, List<FreezeOrderRecPo> foList) {
        foList.stream().forEach(o -> {
            FreezeOrderRecDsQueue.FreezeOrderRec item = new FreezeOrderRecDsQueue.FreezeOrderRec();
            item.setFreezeOrderRec(o).setInsert(true).setType(type);
            this.freezeOrderRecDsQueue.offer(item);
        });
    }

    public void closeFreezeOrderRec(String type, List<FreezeOrderRecPo> foList) {
        foList.stream().forEach(o -> {
            FreezeOrderRecDsQueue.FreezeOrderRec item = new FreezeOrderRecDsQueue.FreezeOrderRec();
            item.setFreezeOrderRec(o).setInsert(true).setType(type);
            this.freezeOrderRecDsQueue.offer(item);
        });
    }
}
