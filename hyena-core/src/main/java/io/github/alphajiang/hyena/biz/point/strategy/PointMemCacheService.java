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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PointMemCacheService {

    //private final String type;
    private Map<String, PointCache> map = new HashMap<>();

    @Autowired
    private PointDs pointDs;


//    public PointMemCacheService(String type, PointDs pointDs) {
//        this.type = type;
//        this.pointDs = pointDs;
//    }

    public PointWrapper getPoint(String type, String uid, boolean lock) {
        PointWrapper result = new PointWrapper(this.getPointX(type, uid));

        if (lock) {
            result.getPointCache().lock();
        }
        if (result.getPointCache().getPoint() == null) {
            result.getPointCache().setPoint(this.pointDs.getPointVo(type, null, uid));
        }
        return result;
    }

    private String formatKey(String type, String uid) {
        return type + "-" + uid;
    }

    private synchronized PointCache getPointX(String type, String uid) {
        String key = this.formatKey(type, uid);
        PointCache p = map.get(key);
        if (p == null) {
            p = new PointCache();
            p.setKey(key).setUpdateTime(new Date());
            map.put(key, p);
        }
        return p;
    }
}
