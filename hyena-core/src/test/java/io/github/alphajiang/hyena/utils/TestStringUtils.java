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

import io.github.alphajiang.hyena.HyenaTestBase;
import org.junit.Assert;
import org.junit.Test;

public class TestStringUtils extends HyenaTestBase {


    @Test
    public void test_upperCase() {
        String str = "abc";
        String result = StringUtils.upperCase(str);
        Assert.assertEquals("ABC", result);
    }

    @Test
    public void test_upperCase_null() {

        String result = StringUtils.upperCase(null);
        HyenaAssert.isNull(result, "result should be null");

    }

    @Test
    public void test_equals() {
        String left = "abc";
        String right = "abc";
        boolean result = StringUtils.equals(left, right);
        HyenaAssert.isTrue(result, "result is true");
    }
}
