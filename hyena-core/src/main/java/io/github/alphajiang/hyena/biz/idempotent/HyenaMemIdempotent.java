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
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Slf4j
public class HyenaMemIdempotent implements HyenaIdempotent {

    private CacheManager cm;


    private List<String> cacheAliases = List.of("increase-point", "decrease-point", "decreaseFrozen-point",
            "freeze-point", "unfreeze-point", "cancel-point", "freeze-by-rec-id",
            "freeze-cost", "unfreeze-cost", "refund");

    @PostConstruct
    public void init() {
        log.info("init ehcache, aliases = {}", cacheAliases);
        cm = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        cacheAliases.forEach(alias -> {
            cm.createCache(alias, CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100))
                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(10L))));

        });

    }

    @Override
    public String getByKey(String name, String seq) {
        Cache<String, String> cache = cm.getCache(name, String.class, String.class);
        return cache.get(seq);
    }

    @Override
    public void setByKey(String name, String seq, BaseResponse obj) {
        cm.getCache(name, String.class, String.class).put(seq, JsonUtils.toJsonString(obj));
    }

    @Override
    public boolean lock(String seq) {
        return true;
    }

    @Override
    public void unlock(String seq) {

    }
}
