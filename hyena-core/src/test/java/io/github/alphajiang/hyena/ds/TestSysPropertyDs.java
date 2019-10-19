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

package io.github.alphajiang.hyena.ds;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.ds.service.SysPropertyDs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestSysPropertyDs extends HyenaTestBase {

    @Autowired
    private SysPropertyDs sysPropertyDs;

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_setSqlVersion() {
        int ver = 10;
        this.sysPropertyDs.setSqlVersion(ver);
        int result = this.sysPropertyDs.getSqlVersion();
        Assertions.assertEquals(ver, result);
    }
}
