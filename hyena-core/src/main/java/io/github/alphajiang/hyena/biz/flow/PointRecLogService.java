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
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PointRecLogService {

    @Autowired
    private PointRecLogDs pointRecLogDs;

    private LinkedBlockingDeque<Wrapper> queue = new LinkedBlockingDeque<>();

    @PostConstruct
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                listener();
            }
        }).start();
    }

    public PointRecLogPo addLogByRec(String type, PointStatus eventType, PointRecPo rec, long delta, String note) {

        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setRecId(rec.getId()).setType(eventType.code())
                .setDelta(delta).setNote(note);
        recLog.setAvailable(rec.getAvailable() == null ? 0L : rec.getAvailable());
        recLog.setUsed(rec.getUsed() == null ? 0L : rec.getUsed());
        recLog.setFrozen(rec.getFrozen() == null ? 0L : rec.getFrozen());
        recLog.setCancelled(rec.getCancelled() == null ? 0L : rec.getCancelled());
        recLog.setExpire(rec.getExpire() == null ? 0L : rec.getExpire());
        recLog.setNote(note == null ? "" : note);
        this.push(type, recLog);
        return recLog;
    }

    private void push(String type, PointRecLogPo recLog) {

        try {
            this.queue.put(new Wrapper(type, recLog));
        } catch (InterruptedException e) {
            log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);
        }
        //this.notify();
    }

    private void listener() {

        do {
            try {
                Wrapper o = queue.poll(1L, TimeUnit.SECONDS);
                if (o != null) {
                    this.pointRecLogDs.addPointRecLog(o.type, o.recLog);
                }
            } catch (InterruptedException e) {
                log.error("queue.size = {}, error = {}", queue.size(), e.getMessage(), e);

            }
        } while (true);


    }

    class Wrapper {
        String type;
        PointRecLogPo recLog;

        public Wrapper(String type, PointRecLogPo recLog) {
            this.type = type;
            this.recLog = recLog;
        }
    }
}
