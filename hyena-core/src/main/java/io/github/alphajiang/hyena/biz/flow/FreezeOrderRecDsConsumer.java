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

import io.github.alphajiang.hyena.ds.service.FreezeOrderRecDs;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class FreezeOrderRecDsConsumer implements Runnable {

    private FreezeOrderRecDs freezeOrderRecDs;
    private LinkedBlockingQueue<FreezeOrderRecDsQueue.FreezeOrderRec> queue;
    private List<FreezeOrderRecDsQueue.FreezeOrderRec> list;

    public FreezeOrderRecDsConsumer(LinkedBlockingQueue<FreezeOrderRecDsQueue.FreezeOrderRec> queue,
                                    FreezeOrderRecDs freezeOrderRecDs) {
        this.list = new ArrayList<>();
        this.queue = queue;
        this.freezeOrderRecDs = freezeOrderRecDs;
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
        FreezeOrderRecDsQueue.FreezeOrderRec pl = queue.poll(1, TimeUnit.SECONDS);
        if (pl == null) {
            return;
        }
        list.clear();
        list.add(pl);
        this.pollMore(list);
        Map<String, List<FreezeOrderRecPo>> map4Insert = new HashMap<>();
        Map<String, List<FreezeOrderRecPo>> map4Update = new HashMap<>();

        list.stream().forEach(o -> {
            if (o.isInsert() && StringUtils.isNotBlank(o.getFreezeOrderRec().getOrderNo())) {
                this.add2Map(o, map4Insert);
            } else {
                this.add2Map(o, map4Update);

            }
        });
        map4Insert.forEach((k, v) -> this.freezeOrderRecDs.batchInsert(k, v));

        map4Update.forEach((k, v) -> this.freezeOrderRecDs.closeByIdList(k, v.stream().map(FreezeOrderRecPo::getId).collect(Collectors.toList())));

    }

    private void add2Map(FreezeOrderRecDsQueue.FreezeOrderRec recIn, Map<String, List<FreezeOrderRecPo>> map) {
        if (!map.containsKey(recIn.getType())) {
            map.put(recIn.getType(), new ArrayList<>());
        }
        map.get(recIn.getType()).add(recIn.getFreezeOrderRec());
    }

    private void pollMore(List<FreezeOrderRecDsQueue.FreezeOrderRec> list) {
        FreezeOrderRecDsQueue.FreezeOrderRec prl = null;
        int max = 100;
        do {
            prl = queue.poll();
            if (prl == null) {
                break;
            }
            list.add(prl);
            max--;
        } while (prl != null && max > 0);
    }
}
