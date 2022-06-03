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

package io.github.alphajiang.hyena.spring.boot.autoconfigure;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class HyenaSwaggerConfiguration {

    @Bean
    public GroupedOpenApi hyenaApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                // 仅显示 io.github.alphajiang.hyena.rest 目录下的接口
//                .apis(RequestHandlerSelectors.basePackage("io.github.alphajiang.hyena.rest"))
//                .build();
        return GroupedOpenApi.builder()
                .group("hyena")
                .packagesToScan("io.github.alphajiang.hyena.rest")
                .build();
    }
}
