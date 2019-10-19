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

import java.util.ArrayList;
import java.util.List;

public class TestCollectionUtils {

    @Test
    public void test_join_null() {
        String result = CollectionUtils.join(null, ",");
        Assertions.assertEquals("", result);
    }

    @Test
    public void test_join_string() {
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add(null);
        list.add("bbb");
        String result = CollectionUtils.join(list, "==");
        Assertions.assertEquals("aaa==bbb", result);
    }

    @Test
    public void test_join_empty() {
        String result = CollectionUtils.join(List.of("", ""), "==");
        Assertions.assertEquals("", result);
    }

    @Test
    public void test_join_char() {
        String result = CollectionUtils.join(List.of(Character.valueOf('a'), Character.valueOf('b')), "-");
        Assertions.assertEquals("a-b", result);
    }

    @Test
    public void test_join_int() {
        List<Integer> list = new ArrayList<>();
        list.add(123);
        list.add(null);
        list.add(456);
        String result = CollectionUtils.join(list, "-");
        Assertions.assertEquals("123-456", result);
    }
}
