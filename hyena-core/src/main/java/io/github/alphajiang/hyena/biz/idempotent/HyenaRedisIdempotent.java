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

package io.github.alphajiang.hyena.biz.idempotent;

import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.github.alphajiang.hyena.utils.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class HyenaRedisIdempotent implements HyenaIdempotent {
    private static final int CACHE_LOCK_TIME_SECONDS = 30;
    private static final int CACHE_TIME_SECONDS = 30 * 60;  // 30 minutes
    private static final String CACHE_KEY_PREFIX = "idempotent-seq-";
    private static final String CACHE_LOCK_KEY_PREFIX = "idempotent-seq-lock-";
    private final Logger logger = LoggerFactory.getLogger(HyenaRedisIdempotent.class);
    private RedisTemplate<String, String> redisStringTemplate;

    public void setRedisTemplate(RedisTemplate<String, String> redisStringTemplate) {
        this.redisStringTemplate = redisStringTemplate;
    }

    @Override
    public String getByKey(String name, String seq) {
        return redisStringTemplate.opsForValue().get(getCacheKey(seq));
    }

    @Override
    public void setByKey(String name, String seq, BaseResponse obj) {
        redisStringTemplate.opsForValue().set(getCacheKey(seq), JsonUtils.toJsonString(obj),
                CACHE_TIME_SECONDS, TimeUnit.SECONDS);
    }


    public boolean lock(String seq) {

        String lockKey = getCacheLockKey(seq);
        HyenaRedisCallback callback = new HyenaRedisCallback(lockKey);

        Boolean ret = redisStringTemplate.execute(callback);
        return ret != null && ret;
    }


    public void unlock(String seq) {
        logger.debug("unlock seq = {}", seq);
        redisStringTemplate.delete(getCacheLockKey(seq));
    }


    private String getCacheKey( String seq) {
        return CACHE_KEY_PREFIX + seq;
    }

    private String getCacheLockKey(String seq) {
        return CACHE_LOCK_KEY_PREFIX + seq;
    }

    public static class HyenaRedisCallback implements RedisCallback<Boolean> {

        private String lockKey;

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
