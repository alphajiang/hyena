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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    private ReactiveStringRedisTemplate redisStringTemplate;

    @PostConstruct
    public void init() {
        hyenaCacheFactory.setPointCacheService(this);
    }

    public String getCacheType() {
        return CACHE_TYPE_REDIS;
    }


    @Override
    public Mono<PointWrapper> getPoint(String type, String uid, String subUid, boolean lock) {
        int retry = 5;
        if (!lock) {
            retry = 0;
        }
        Mono<PointWrapper> mono = lockLoop(type, uid, subUid, retry)
                .flatMap(x -> this.getPointX(type, uid, subUid)
                        .map(pc -> new PointWrapper(pc))
                        .doOnNext(c -> {
                            if (lock) {
                                c.getPointCache().lock();
                            }
                        })
                        .flatMap(c -> {
                            if (c.getPointCache().getPoint() == null) {
                                PointVo p = this.pointDs.getPointVo(type, null, uid, subUid);
                                if (p != null && p.getRecList() != null) {
                                    p.setRecList(p.getRecList().stream().sorted(Comparator.comparingLong(PointRecPo::getId)).collect(Collectors.toList()));
                                }
                                c.getPointCache().setPoint(p);
                                if(p != null) {
                                    return updateRedisPointCache(type, c.getPointCache().getPoint())
                                            .map(b -> c);
                                }else {
                                    return Mono.just(c);
                                }
                            } else {
                                return Mono.just(c);
                            }
                        })
                );
        return mono;
    }

    private Mono<Boolean> updateRedisPointCache(String type, PointVo point) {
        String key = formatKey(type, point.getUid(), point.getSubUid());
        return this.redisStringTemplate.opsForValue().set(key, JsonUtils.toJsonString(point), Duration.ofMinutes(this.cacheTtl));
    }

    @Override
    public Mono<PointVo> updatePoint(String type, String uid, String subUid, PointVo point) {
        String key = formatKey(type, uid, subUid);
        String lockKey = formatLockKey(type, uid, subUid);
        log.info("update-unlock. key = {}, lockKey = {}, point = {}",
                key, lockKey, point);
        return this.redisStringTemplate.opsForValue().set(key, JsonUtils.toJsonString(point), Duration.ofMinutes(this.cacheTtl))
                .flatMap(rt -> redisStringTemplate.delete(lockKey).map(delRt -> {
                    log.debug("update-unlock done. key = {}, lockKey = {}", key, lockKey);
                    return delRt > 0L;
                }))
                .map(rt -> point);
    }

    @Override
    public Mono<Boolean> removePoint(String type, String uid, String subUid) {
//        try {
        String key = this.formatKey(type, uid, subUid);
        return redisStringTemplate.delete(key)
                .map(rt -> {
                    log.info("removePoint rt = {}", rt);
                    return Boolean.TRUE;
                });
//        } catch (Exception e) {
//            log.error("can't remove point. type = {}, uid = {}", type, uid);
//        }
    }


    private Mono<PointCache> getPointX(String type, String uid, String subUid) {
        String key = this.formatKey(type, uid, subUid);
        return redisStringTemplate.opsForValue().get(key)
                .flatMap(strVal -> {
//                    log.debug("strVal from redis : {}", strVal);
                    PointCache p = new PointCache();
                    if (StringUtils.isNotBlank(strVal)) {
                        p.setPoint(JsonUtils.fromJson(strVal, PointVo.class));
                    }
                    return Mono.just(p);
                })
                .switchIfEmpty(Mono.just(new PointCache()))
                .doOnNext(pt -> {
                    pt.setUpdateTime(new Date());
                });

    }

    @Override
    public Collection<PointCache> dump() {
        return List.of();
    }

    @Override
    public void expireCache() {

    }

    private long getExpireTime() {
        return System.currentTimeMillis() + CACHE_LOCK_TIME_SECONDS * 1000 + 1;
    }


    public Mono<Boolean> lockLoop(String type, String uid, String subUid, int retry) {
        if (retry < 1) {
            return Mono.just(Boolean.TRUE);
        }
        Flux<Boolean> flux = Flux.empty();
        for (int i = 0; i < retry; i++) {
            int retryX = retry - i;
            if (i == 0) {
                flux = flux.concatWith(lock(type, uid, subUid, retryX));
            } else {
                flux = flux.concatWith(Mono.delay(Duration.ofMillis(50L)).flatMap(o -> lock(type, uid, subUid, retryX)));
            }
        }
        return flux.any(Boolean.TRUE::equals)
                .doOnNext(ret -> {
                    if (!ret) {
                        log.error("failed to get cache. type = {}, uid = {}, subUid = {}", type, uid, subUid);
                        throw new HyenaServiceException("failed to get cache");
                    }
                });
    }

    public Mono<Boolean> lock(String type, String uid, String subUid, int retry) {

        String lockKey = formatLockKey(type, uid, subUid);
        log.debug("lockKey = {}, retry = {}", lockKey, retry);
        Flux<Object> flux = redisStringTemplate.executeInSession(op -> {
            return op.opsForValue().setIfAbsent(lockKey, String.valueOf(getExpireTime()))
                    .flatMap(ret -> {
                        log.debug("lockKey = {}, setNX ret = {}", lockKey, ret);
                        if (Boolean.TRUE.equals(ret)) {
                            return op.expireAt(lockKey, Instant.ofEpochMilli(getExpireTime()));
//                            return op.expireAt(lockKey, Instant.ofEpochMilli(System.currentTimeMillis() + 90 * 1000 + 1));
                        } else {
                            return op.opsForValue().get(lockKey)
                                    .flatMap(v -> {
                                        long expireTime = NumberUtils.parseLong(v, 0L);
                                        log.debug("lockKey = {}, expireTime = {}", lockKey, expireTime);
                                        if (expireTime < System.currentTimeMillis()) {
                                            return op.opsForValue().getAndSet(lockKey, String.valueOf(getExpireTime()))
                                                    .map(oldVal -> {
                                                        long oldExpire = NumberUtils.parseLong(oldVal, 0L);
                                                        log.debug("lockKey = {}, oldExpire = {}, curtime = {}", lockKey, oldExpire, System.currentTimeMillis());
                                                        return oldExpire < System.currentTimeMillis();
                                                    });
                                        } else {
                                            return Mono.just(Boolean.FALSE);
                                        }
                                    });
                        }
                    });

        });
        return flux.all(ret -> Boolean.TRUE.equals(ret));
    }

    @Override
    public Mono<Boolean> unlock(String type, String uid, String subUid) {
        //log.debug("unlock seq = {}", seq);
        String lockKey = formatLockKey(type, uid, subUid);
        return redisStringTemplate.delete(lockKey)
                .map(rt -> {
                    log.debug("unlock lockKey = {}, rt = {}", lockKey, rt);
                    return Boolean.TRUE;
                });

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

}
