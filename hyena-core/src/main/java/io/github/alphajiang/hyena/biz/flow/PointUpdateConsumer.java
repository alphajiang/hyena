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

import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.model.po.PointPo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PointUpdateConsumer implements Runnable {

    private PointDs pointDs;
    private LinkedBlockingQueue<PointUpdateQueue.Point> queue;
    private List<PointUpdateQueue.Point> list;

    public PointUpdateConsumer(LinkedBlockingQueue<PointUpdateQueue.Point> queue,
                               PointDs pointDs) {
        this.list = new ArrayList<>();
        this.queue = queue;
        this.pointDs = pointDs;
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

        PointUpdateQueue.Point pl = queue.poll(1, TimeUnit.SECONDS);
        if (pl == null) {
            return;
        }
        list.clear();
        list.add(pl);
        this.pollMore(list);
        Map<String, List<PointPo>> map = new HashMap<>();
        list.stream().forEach(o -> {
            if (!map.containsKey(o.getType())) {
                map.put(o.getType(), new ArrayList<>());
            }
            map.get(o.getType()).add(o.getPoint());
        });
        map.forEach((k, v) -> this.pointDs.batchUpdate(k, v));
    }

    private void pollMore(List<PointUpdateQueue.Point> list) {
        PointUpdateQueue.Point pl = null;
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
