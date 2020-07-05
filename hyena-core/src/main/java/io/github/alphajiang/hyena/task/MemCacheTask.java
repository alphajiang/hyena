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

package io.github.alphajiang.hyena.task;

import io.github.alphajiang.hyena.biz.cache.PointMemCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemCacheTask {

    @Value("${hyena.mem.task.duration:10}")
    private long expireRate;

    private long idx = 0L;

    @Autowired
    private PointMemCacheService pointMemCacheService;

    @Scheduled(fixedRate = 60 * 1000, initialDelay = 30 * 1000)  // every minutes
    public void memCacheTask() {
        //log.debug(">>");
        if (idx % expireRate == 0) {
            pointMemCacheService.expireCache();
        }
        idx++;
    }
}
