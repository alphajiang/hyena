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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PointRecDsConsumer implements Runnable {

    private PointRecDs pointRecDs;
    private LinkedBlockingQueue<PointRecDsQueue.PointRec> queue;
    private List<PointRecDsQueue.PointRec> list;

    public PointRecDsConsumer(LinkedBlockingQueue<PointRecDsQueue.PointRec> queue,
                              PointRecDs pointRecDs) {
        this.list = new ArrayList<>();
        this.queue = queue;
        this.pointRecDs = pointRecDs;
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

        PointRecDsQueue.PointRec pl = queue.poll(1, TimeUnit.SECONDS);
        if (pl == null) {
            return;
        }
        list.clear();
        list.add(pl);
        this.pollMore(list);
        Map<String, List<PointRecPo>> map4Insert = new HashMap<>();
        Map<String, List<PointRecPo>> map4Update = new HashMap<>();
        list.stream().forEach(o -> {
            if (o.isInsert()) {
                this.add2Map(o, map4Insert);
            } else {
                this.add2Map(o, map4Update);
            }
        });
        map4Insert.forEach((k, v) -> this.pointRecDs.batchInsert(k, v));
        map4Update.forEach((k, v) -> this.pointRecDs.batchUpdate(k, v));
    }

    private void add2Map(PointRecDsQueue.PointRec recIn, Map<String, List<PointRecPo>> map) {
        if (!map.containsKey(recIn.getType())) {
            map.put(recIn.getType(), new ArrayList<>());
        }
        map.get(recIn.getType()).add(recIn.getPointRec());
    }


    private void pollMore(List<PointRecDsQueue.PointRec> list) {
        PointRecDsQueue.PointRec pl = null;
        int max = 100;
        do {
            pl = queue.poll();
            if (pl == null) {
                break;
            }
            list.add(pl);
            max--;
        } while (pl != null && max > 0);
    }
}
