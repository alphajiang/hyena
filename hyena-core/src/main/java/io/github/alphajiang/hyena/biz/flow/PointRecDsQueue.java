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

import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class PointRecDsQueue implements PointDsQueue {


    private LinkedBlockingQueue<PointRec> queue;

    private PointRecDsConsumer consumer;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private QueueMonitor queueMonitor;

    @PostConstruct
    public void init() {
        queue = new LinkedBlockingQueue<>();
        consumer = new PointRecDsConsumer(queue, pointRecDs);
        new Thread(consumer).start();
        queueMonitor.addQueue(this);
    }

    public boolean offer(PointRec pl) {
        return this.queue.offer(pl);
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class PointRec extends QueueItem {
        private PointRecPo pointRec;
    }
}
