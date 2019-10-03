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

import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PointLogFlowConsumer implements Runnable {

    private PointLogDs pointLogDs;
    private LinkedBlockingQueue<PointLogFlowQueue.PointLog> queue;
    private List<PointLogFlowQueue.PointLog> list;

    public PointLogFlowConsumer(LinkedBlockingQueue<PointLogFlowQueue.PointLog> queue,
                                PointLogDs pointLogDs) {
        this.list = new ArrayList<>();
        this.queue = queue;
        this.pointLogDs = pointLogDs;
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

        PointLogFlowQueue.PointLog pl = queue.poll(1, TimeUnit.SECONDS);
        if (pl == null) {
            return;
        }
        list.clear();
        list.add(pl);
        this.pollMore(list);
        Map<String, List<PointLogPo>> map = new HashMap<>();
        list.stream().forEach(o -> {
            if (!map.containsKey(o.getType())) {
                map.put(o.getType(), new ArrayList<>());
            }
            map.get(o.getType()).add(o.getPointLog());
        });
        map.forEach((k, v) -> this.pointLogDs.batchInsert(k, v));
    }

    private void pollMore(List<PointLogFlowQueue.PointLog> list) {
        PointLogFlowQueue.PointLog pl = null;
        int max = 100;
        do {
            pl = queue.poll();
            if(pl == null) {
                break;
            }
            list.add(pl);
            max--;
        } while (pl != null && max > 0);
    }
}
