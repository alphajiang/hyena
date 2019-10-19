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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestNumberUtils {

    @Test
    public void test_parseLong_bytes() {
        String str = "12345";
        long result = NumberUtils.parseLong(str.getBytes(), 0L);
        Assertions.assertEquals(12345L, result);
    }

    @Test
    public void test_parseLong_bytes_null() {
        byte[] in = null;
        long result = NumberUtils.parseLong(in, 123L);
        Assertions.assertEquals(123L, result);
    }

    @Test
    public void test_parseLong_not_number() {
        String str = "123ab";
        long result = NumberUtils.parseLong(str, 123L);
        Assertions.assertEquals(123L, result);
    }
}
