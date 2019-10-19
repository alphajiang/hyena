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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.exception.HyenaStatusException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

@Slf4j
public class TestHyenaAssert {

    @Test
    public void test_isTrue() {
        Assertions.assertThrows(HyenaStatusException.class, () -> {
            HyenaAssert.isTrue(1 == 2, "expect HyenaStatusException");
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }

    @Test
    public void test_isTrue_exception() {
        Assertions.assertThrows(HyenaStatusException.class, () -> {
            HyenaAssert.isTrue(1 == 2, HyenaConstants.RES_CODE_STATUS_ERROR, "expect HyenaStatusException",
                    Level.INFO, HyenaStatusException.class);
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }


    @Test
    public void test_equals() {
        Assertions.assertThrows(HyenaServiceException.class, () -> {
            HyenaAssert.equals("aaa", "bbb", "expect HyenaServiceException");
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }


    @Test
    public void test_isNull() {
        Integer obj = Integer.parseInt("123");
        Assertions.assertThrows(HyenaParameterException.class, () -> {
            HyenaAssert.isNull(obj, "expect HyenaParameterException");
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }

    @Test
    public void test_isNull_exception() {
        Integer obj = Integer.parseInt("123");
        Assertions.assertThrows(HyenaServiceException.class, () -> {
            HyenaAssert.isNull(obj, HyenaConstants.RES_CODE_SERVICE_ERROR,
                    "expect HyenaParameterException", Level.DEBUG,
                    HyenaServiceException.class);
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }

    @Test
    public void test_notNull() {
        Integer obj = null;
        Assertions.assertThrows(HyenaParameterException.class, () -> {
            HyenaAssert.notNull(obj, "expect HyenaParameterException");
            log.error("test case fail!!!");
            Assertions.fail();
        });
    }
}
