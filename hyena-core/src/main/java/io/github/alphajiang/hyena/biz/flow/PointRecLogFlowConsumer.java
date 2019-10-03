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

import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PointRecLogFlowConsumer implements Runnable {

    private PointRecLogDs pointRecLogDs;
    private LinkedBlockingQueue<PointRecLogFlowQueue.PointRecLog> queue;
    private List<PointRecLogFlowQueue.PointRecLog> list;

    public PointRecLogFlowConsumer(LinkedBlockingQueue<PointRecLogFlowQueue.PointRecLog> queue,
                                   PointRecLogDs pointRecLogDs) {
        this.list = new ArrayList<>();
        this.queue = queue;
        this.pointRecLogDs = pointRecLogDs;
    }

    @Override
    public void run() {
        do {
            try {
                this.threadLoop();
            } catch (Exception e) {
                log.warn("error = {}", e.getMessage(), e);
            }
        } while (true);
    }

    private void threadLoop() throws Exception {

        PointRecLogFlowQueue.PointRecLog prl = queue.poll(1, TimeUnit.SECONDS);
        if (prl == null) {
            return;
        }
        list.clear();
        list.add(prl);
        this.pollMore(list);
        Map<String, List<PointRecLogPo>> map = new HashMap<>();
        list.stream().forEach(o -> {
            if (!map.containsKey(o.getType())) {
                map.put(o.getType(), new ArrayList<>());
            }
            map.get(o.getType()).add(o.getPointRecLog());
        });
        map.forEach((k, v) -> this.pointRecLogDs.batchInsert(k, v));
    }

    private void pollMore(List<PointRecLogFlowQueue.PointRecLog> list) {
        PointRecLogFlowQueue.PointRecLog prl = null;
        int max = 100;
        do {
            prl = queue.poll();
            if(prl == null) {
                break;
            }
            list.add(prl);
            max--;
        } while (prl != null && max > 0);
    }
}
