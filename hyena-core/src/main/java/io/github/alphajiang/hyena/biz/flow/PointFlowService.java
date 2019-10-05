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
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
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


    public void addFlow(PointUsage usage, PointLogPo pointLog, List<PointRecLogPo> recLogs) {
        this.pointLogFlowQueue.offer(new PointLogFlowQueue.PointLog(usage.getType(), pointLog));
        recLogs.stream().forEach(o -> this.pointRecLogFlowQueue.offer(new PointRecLogFlowQueue.PointRecLog(usage.getType(), o)));

    }

    public void updatePoint(String type, PointPo point) {
        this.pointUpdateQueue.offer(new PointUpdateQueue.Point(type, point));
    }

//    public void insertPointRec(String type, PointRecPo rec) {
//        this.pointRecDsQueue.offer(new PointRecDsQueue.PointRec(true, type, rec));
//    }

    public void updatePointRec(String type, List<PointRecPo> recList) {
        recList.stream().forEach(o -> this.pointRecDsQueue.offer(new PointRecDsQueue.PointRec(false, type, o)));
    }


}
