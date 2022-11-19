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

package io.github.alphajiang.hyena.biz.cache;

import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointVo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PointMemCacheService implements IPointCache {

    private static final String CACHE_TYPE_MEMORY = "memory";
    //private final String type;
    private final Map<String, PointCache> map = new ConcurrentHashMap<>();
    @Value("${hyena.mem.ttl:30}")
    private int cacheTtl; // minutes
    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private PointDs pointDs;


    @PostConstruct
    public void init() {
        hyenaCacheFactory.setPointCacheService(this);
    }

    public String getCacheType() {
        return CACHE_TYPE_MEMORY;
    }

    @Override
    public Mono<PointWrapper> getPoint(String type, String uid, String subUid, boolean lock) {
        PointWrapper result = new PointWrapper(this.getPointX(type, uid, subUid));

        if (lock) {
            long startTime = System.nanoTime();
            result.getPointCache().lock();
            long endTime = System.nanoTime();
            long lockMs = (endTime - startTime) / 1000 * 1000; // 获得锁消耗的时间
            if (lockMs > 500) {
                log.info("key = {}, lock takes {} ms", result.getPointCache().getKey(), lockMs);
            }
        }
        if (result.getPointCache().getPoint() == null) {
            PointVo p = this.pointDs.getPointVo(type, null, uid, subUid);
            if (p != null && p.getRecList() != null) {
                p.setRecList(
                    p.getRecList().stream().sorted(Comparator.comparingLong(PointRecPo::getId))
                        .collect(Collectors.toList()));
            }
            result.getPointCache().setPoint(p);
        }
        return Mono.just(result);
    }

    @Override
    public Mono<Boolean> removePoint(String type, String uid, String subUid) {
        return Mono.just(this.formatKey(type, uid, subUid))
            .map(key -> {
                map.remove(key);
                return Boolean.TRUE;
            });
    }

    private String formatKey(String type, String uid, String subUid) {
        if (subUid == null) {
            return type + "-" + uid;
        } else {
            return type + "-" + uid + "-" + subUid;
        }
    }

    private synchronized PointCache getPointX(String type, String uid, String subUid) {
        String key = this.formatKey(type, uid, subUid);
        PointCache p = map.get(key);
        if (p == null) {
            p = new PointCache();
            p.setKey(key);
            map.put(key, p);
        }
        p.setUpdateTime(new Date());
        return p;
    }

    @Override
    public Collection<PointCache> dump() {
        return map.values();
    }

    @Override
    public void expireCache() {
        if (map.isEmpty()) {
            return;
        }
        Calendar calExpire = Calendar.getInstance();
        calExpire.add(Calendar.MINUTE, -cacheTtl);
        Date expire = calExpire.getTime();
        List<String> keyList = new ArrayList<>();
        map.forEach((k, v) -> {
            if (v.getUpdateTime().before(expire)) {
                keyList.add(k);
            }
        });
        if (!keyList.isEmpty()) {
            log.info("start remove the mem cache keys: {}", keyList);
            keyList.stream().forEach(k -> map.remove(k));
            log.info("end remove mem cache");
        }
    }

    @Override
    public Mono<PointVo> updatePoint(String type, String uid, String subUid, PointVo point) {
        return Mono.just(new PointCache())
            .doOnNext(p -> {
                String key = formatKey(type, uid, subUid);
                p.setKey(key);
                p.setPoint(point);
                p.setUpdateTime(new Date());
                map.put(key, p);
            })
            .map(pc -> pc.getPoint());

    }

    @Override
    public Mono<Boolean> unlock(String type, String uid, String subUid) {
        return Mono.just(Boolean.TRUE);
    }

}
