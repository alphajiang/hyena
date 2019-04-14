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

package io.github.alphajiang.hyena.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJsonUtils extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestJsonUtils.class);

    @Test
    public void test_fromJson() {
        var obj = new BaseResponse(123, "gewlgejwg");
        String str = obj.toJsonString();
        logger.info("json str = {}", str);
        BaseResponse result = JsonUtils.fromJson(str, BaseResponse.class);
        HyenaAssert.notNull(result, "result is not null");
    }

    @Test(expected = HyenaServiceException.class)
    public void test_fromJson_class_fail() {

        String str = "{ 123}";
        BaseResponse result = JsonUtils.fromJson(str, BaseResponse.class);
        Assert.fail();
    }

    @Test(expected = HyenaServiceException.class)
    public void test_fromJson_type_fail() {

        String str = "{ 123}";
        ObjectResponse<String> result = JsonUtils.fromJson(str, new TypeReference<ObjectResponse<String>>() {

        });
        Assert.fail();
    }
}
