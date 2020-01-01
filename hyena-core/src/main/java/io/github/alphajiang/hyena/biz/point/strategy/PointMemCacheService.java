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

package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PointMemCacheService {


    @Value("${hyena.mem.ttl:30}")
    private int cacheTtl; // minutes

    //private final String type;
    private Map<String, PointCache> map = new ConcurrentHashMap<>();

    @Autowired
    private PointDs pointDs;


    public PointWrapper getPoint(String type, String uid, String subUid, boolean lock) {
        PointWrapper result = new PointWrapper(this.getPointX(type, uid, subUid));

        if (lock) {
            result.getPointCache().lock();
        }
        if (result.getPointCache().getPoint() == null) {
            PointVo p = this.pointDs.getPointVo(type, null, uid, subUid);
            if (p != null && p.getRecList() != null) {
                p.setRecList(p.getRecList().stream().sorted(Comparator.comparingLong(PointRecPo::getId)).collect(Collectors.toList()));
            }
            result.getPointCache().setPoint(p);
        }
        return result;
    }

    public synchronized void removePoint(String type, String uid, String subUid) {
        try {
            String key = this.formatKey(type, uid, subUid);
            map.remove(key);
        } catch (Exception e) {
            log.error("can't remove point. type = {}, uid = {}", type, uid);
        }
    }

    private String formatKey(String type, String uid, String subUid) {
        if(subUid == null) {
            return type + "-" + uid;
        }else {
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

    public Collection<PointCache> dump() {
        return map.values();
    }

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
}
