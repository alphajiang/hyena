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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


public class TestHyenaAssert {
    private final Logger logger = LoggerFactory.getLogger(TestHyenaAssert.class);

    @Test(expected = HyenaStatusException.class)
    public void test_isTrue() {
        HyenaAssert.isTrue(1 == 2, "expect HyenaStatusException");
        logger.error("test case fail!!!");
        Assert.fail();
    }

    @Test(expected = HyenaStatusException.class)
    public void test_isTrue_exception() {
        HyenaAssert.isTrue(1 == 2, HyenaConstants.RES_CODE_STATUS_ERROR, "expect HyenaStatusException",
                Level.INFO, HyenaStatusException.class);
        logger.error("test case fail!!!");
        Assert.fail();
    }


    @Test(expected = HyenaServiceException.class)
    public void test_equals() {
        HyenaAssert.equals("aaa", "bbb", "expect HyenaServiceException");
        logger.error("test case fail!!!");
        Assert.fail();
    }


    @Test(expected = HyenaParameterException.class)
    public void test_isNull() {
        Integer obj = Integer.parseInt("123");
        HyenaAssert.isNull(obj, "expect HyenaParameterException");
        logger.error("test case fail!!!");
        Assert.fail();
    }

    @Test(expected = HyenaServiceException.class)
    public void test_isNull_exception() {
        Integer obj = Integer.parseInt("123");
        HyenaAssert.isNull(obj, HyenaConstants.RES_CODE_SERVICE_ERROR,
                "expect HyenaParameterException", Level.DEBUG,
                HyenaServiceException.class);
        logger.error("test case fail!!!");
        Assert.fail();
    }

    @Test(expected = HyenaParameterException.class)
    public void test_notNull() {
        Integer obj = null;
        HyenaAssert.notNull(obj, "expect HyenaParameterException");
        logger.error("test case fail!!!");
        Assert.fail();
    }
}
