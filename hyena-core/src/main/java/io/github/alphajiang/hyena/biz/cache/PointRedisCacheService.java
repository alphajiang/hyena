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
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.vo.PointVo;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.github.alphajiang.hyena.utils.NumberUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PointRedisCacheService implements IPointCache {

    private static final int CACHE_LOCK_TIME_SECONDS = 30;
    private static final String CACHE_TYPE_REDIS = "redis";
    private static final String CACHE_LOCK_KEY_FORMAT = "pt.{0}.{1}{2}{3}.lock";
    private static final String POINT_CACHE_KEY = "pt.{0}.{1}{2}{3}";

    @Value("${hyena.mem.ttl:30}")
    private int cacheTtl; // minutes


    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private PointDs pointDs;

    @Autowired
    private RedisTemplate<String, String> redisStringTemplate;

    @PostConstruct
    public void init() {
        hyenaCacheFactory.setPointCacheService(this);
    }

    public String getCacheType() {
        return CACHE_TYPE_REDIS;
    }


//    public void setRedisTemplate(RedisTemplate<String, String> redisStringTemplate) {
//        this.redisStringTemplate = redisStringTemplate;
//    }


    @Override
    public PointWrapper getPoint(String type, String uid, String subUid, boolean lock) {
        if (lock) {
            int retry = 5;
            boolean locked = false;
            while (!locked && retry > 0) {
                locked = this.lock(type, uid, subUid);
                retry--;
                if (!locked && retry > 0) {
                    try {
                        this.wait(50, 0);
                    } catch (Exception e) {
                        log.warn("error = {}", e.getMessage(), e);
                    }
                }
            }
            if (!locked) {
                log.error("failed to get cache. type = {}, uid = {}, subUid = {}", type, uid, subUid);
                throw new HyenaServiceException("failed to get cache");
            }
        }
        PointWrapper result = new PointWrapper(this.getPointX(type, uid, subUid));
        result.getPointCache().lock();
        if (result.getPointCache().getPoint() == null) {
            PointVo p = this.pointDs.getPointVo(type, null, uid, subUid);
            if (p != null && p.getRecList() != null) {
                p.setRecList(p.getRecList().stream().sorted(Comparator.comparingLong(PointRecPo::getId)).collect(Collectors.toList()));
            }
            result.getPointCache().setPoint(p);
            String key = formatKey(type, uid, subUid);
            this.redisStringTemplate.opsForValue().set(key,
                    JsonUtils.toJsonString(result.getPointCache().getPoint()));
            this.redisStringTemplate.expire(key, this.cacheTtl, TimeUnit.MINUTES);
        }
        return result;
    }

    @Override
    public void updatePoint(String type, String uid, String subUid, PointVo point) {
        String key = formatKey(type, uid, subUid);
        log.info("key = {}, point = {}", key, point);
        this.redisStringTemplate.opsForValue().set(key, JsonUtils.toJsonString(point));
        this.redisStringTemplate.expire(key, this.cacheTtl, TimeUnit.MINUTES);
        this.unlock(type, uid, subUid);
    }

    @Override
    public synchronized void removePoint(String type, String uid, String subUid) {
        try {
            String key = this.formatKey(type, uid, subUid);
            redisStringTemplate.delete(key);
        } catch (Exception e) {
            log.error("can't remove point. type = {}, uid = {}", type, uid);
        }
    }


    private synchronized PointCache getPointX(String type, String uid, String subUid) {
        String key = this.formatKey(type, uid, subUid);
        PointCache p = new PointCache();
        String strVal = redisStringTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(strVal)) {
            //p = new PointCache();
            //p.setKey(key);
        } else {
            p.setPoint(JsonUtils.fromJson(strVal, PointVo.class));
        }
        p.setUpdateTime(new Date());
        return p;
    }

    @Override
    public Collection<PointCache> dump() {
        return List.of();
    }

    @Override
    public void expireCache() {

    }


    public boolean lock(String type, String uid, String subUid) {

        String lockKey = formatLockKey(type, uid, subUid);
        PointRedisCacheService.HyenaRedisCallback callback = new PointRedisCacheService.HyenaRedisCallback(lockKey);

        Boolean ret = redisStringTemplate.execute(callback);
        return ret != null && ret;
    }

    @Override
    public void unlock(String type, String uid, String subUid) {
        //log.debug("unlock seq = {}", seq);
        redisStringTemplate.delete(formatLockKey(type, uid, subUid));
    }

    private String formatKey(String type, String uid, String subUid) {
        if (StringUtils.isBlank(subUid)) {
            return MessageFormat.format(POINT_CACHE_KEY, type, uid, "", "");
        } else {
            return MessageFormat.format(POINT_CACHE_KEY, type, uid, ".", subUid);
        }
    }

    private String formatLockKey(String type, String uid, String subUid) {
        if (StringUtils.isBlank(subUid)) {
            return MessageFormat.format(CACHE_LOCK_KEY_FORMAT, type, uid, "", "");
        } else {
            return MessageFormat.format(CACHE_LOCK_KEY_FORMAT, type, uid, ".", subUid);
        }
    }

    public static class HyenaRedisCallback implements RedisCallback<Boolean> {

        private final String lockKey;

        HyenaRedisCallback(String lockKey) {
            this.lockKey = lockKey;
        }

        @Override
        public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
            Boolean acquire = connection.setNX(lockKey.getBytes(), String.valueOf(getExpireTime()).getBytes());

            if (acquire != null && acquire) {
                connection.pExpireAt(lockKey.getBytes(), getExpireTime());
                return true;
            } else {

                byte[] value = connection.get(lockKey.getBytes());

                if (value != null && value.length > 0) {

                    long expireTime = Long.parseLong(new String(value));

                    if (expireTime < System.currentTimeMillis()) {
                        long oldValue = NumberUtils.parseLong(connection.getSet(lockKey.getBytes(),
                                String.valueOf(getExpireTime()).getBytes()), 0L);
                        return oldValue < System.currentTimeMillis();
                    }
                }
            }
            return false;
        }

        private long getExpireTime() {
            return System.currentTimeMillis() + CACHE_LOCK_TIME_SECONDS * 1000 + 1;
        }
    }
}
