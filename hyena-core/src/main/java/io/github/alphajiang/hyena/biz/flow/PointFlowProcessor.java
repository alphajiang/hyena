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
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class PointFlowProcessor implements Runnable {


    private LinkedBlockingDeque<PointFlowWrapper> queue = new LinkedBlockingDeque<>();
    private AtomicLong queueSize = new AtomicLong(0L);
    private PointFlowStrategyFactory pointFlowStrategyFactory;


    public PointFlowProcessor(PointFlowStrategyFactory pointFlowStrategyFactory){
        this.pointFlowStrategyFactory = pointFlowStrategyFactory;
    }

    public void push(CalcType calcType, PointUsage usage, PointPo point) {
        try {
            this.queue.put(new PointFlowWrapper(calcType, usage, point));
            queueSize.addAndGet(1L);
        } catch (InterruptedException e) {
            log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
        }
    }


    @Override
    public void run() {
        do {
            try {
                PointFlowWrapper o = queue.poll(1L, TimeUnit.SECONDS);
                if (o != null) {
                    long size = queueSize.addAndGet(-1L);
                    if(size > 100 && size % 100 == 0) {
                        log.warn("堆积过大. size = {}", size);
                    }
                    pointFlowStrategyFactory.addFlow(o);
//                    Optional<PointFlowStrategy> strategy = pointFlowStrategyFactory.getStrategy(o.getCalcType());
//                    strategy.ifPresent(act -> act.addFlow(o.getUsage(), o.getPoint()));
                }
            } catch (InterruptedException e) {
                log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);

            }
        } while (true);
    }
}
