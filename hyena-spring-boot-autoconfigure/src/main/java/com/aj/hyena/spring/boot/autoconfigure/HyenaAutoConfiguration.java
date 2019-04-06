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

package com.aj.hyena.spring.boot.autoconfigure;

import com.aj.hyena.biz.idempotent.HyenaIdempotent;
import com.aj.hyena.biz.idempotent.HyenaMemIdempotent;
import com.aj.hyena.biz.idempotent.HyenaRedisIdempotent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableConfigurationProperties(HyenaProperties.class)
public class HyenaAutoConfiguration implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(HyenaAutoConfiguration.class);
    private final HyenaProperties properties;
    @Autowired
    private RedisTemplate<String, String> redisStringTemplate;

    public HyenaAutoConfiguration(HyenaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("hyena.idempotent = {}", properties.getIdempotent());
    }

    @Bean(name = "hyenaIdempotent")
    public HyenaIdempotent hyenaIdempotent() {
        HyenaIdempotent idempotent = null;
        if ("redis".equalsIgnoreCase(properties.getIdempotent())) {
            idempotent = new HyenaRedisIdempotent();
            ((HyenaRedisIdempotent) idempotent).setRedisTemplate(redisStringTemplate);

        } else {
            idempotent = new HyenaMemIdempotent();
        }
        return idempotent;
    }


}
