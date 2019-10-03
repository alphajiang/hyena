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
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.vo.QueueInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class PointFlowProcessor implements Runnable {


    private LinkedBlockingDeque<PointFlowWrapper> queue = new LinkedBlockingDeque<>();
    private final String name;
    private long maxSize = 0L;
    private AtomicLong queueSize = new AtomicLong(0L);
    private PointFlowStrategyFactory pointFlowStrategyFactory;



    public PointFlowProcessor(PointFlowStrategyFactory pointFlowStrategyFactory, String name){
        this.pointFlowStrategyFactory = pointFlowStrategyFactory;
        this.name = name;
    }

    public QueueInfo getQueueInfo() {
        QueueInfo ret = new QueueInfo();
        ret.setCurSize(queueSize.get()).setMaxSize(this.maxSize).setName(this.name);
        return ret;
    }

    @Deprecated
    public void push(CalcType calcType, PointUsage usage, PointPo point) {
        try {
            this.queue.put(new PointFlowWrapper(calcType, usage, point, null, null, 5));
            long curSize = queueSize.addAndGet(1L);
            if(curSize > maxSize) {
                maxSize = curSize;
            }
        } catch (InterruptedException e) {
            log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
        }
    }


//    public void push(CalcType calcType, PointUsage usage, PointPo point,
//                     PointLogPo pointLog, List<PointRecLogPo> recLogs) {
//        try {
//            this.queue.put(new PointFlowWrapper(calcType, usage, point,  pointLog, recLogs, 5));
//            long curSize = queueSize.addAndGet(1L);
//            if(curSize > maxSize) {
//                maxSize = curSize;
//            }
//        } catch (InterruptedException e) {
//            log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
//        }
//    }

    @Override
    public void run() {
        do {
            PointFlowWrapper o =  null;
            try {
                o = queue.poll(1L, TimeUnit.SECONDS);
                if (o != null) {
                    long size = queueSize.addAndGet(-1L);
                    if(size > 100 && size % 100 == 0) {
                        log.warn("堆积过大. size = {}", size);
                    }
                    pointFlowStrategyFactory.addFlow(o);
//                    Optional<PointFlowStrategy> strategy = pointFlowStrategyFactory.getStrategy(o.getCalcType());
//                    strategy.ifPresent(act -> act.addFlow(o.getUsage(), o.getPoint()));
                }
            } catch (Exception e) {
                log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
                if(o != null) {
                    try {
                        if(o.getRetry() > 0) {
                            o.setRetry(o.getRetry() -1);
                            queue.put(o);
                            queueSize.addAndGet(1L);
                        }
                    } catch (InterruptedException ex) {
                        log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
                    }
                }
            }
        } while (true);
    }
}
