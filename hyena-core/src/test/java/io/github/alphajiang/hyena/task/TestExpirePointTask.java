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

package io.github.alphajiang.hyena.task;

import io.github.alphajiang.hyena.HyenaTestBase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

public class TestExpirePointTask extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestExpirePointTask.class);

    @Autowired
    private ExpirePointTask expirePointTask;


    @Before
    public void init() {
        Calendar calExpire = Calendar.getInstance();
        calExpire.add(Calendar.MINUTE, -1);
        super.getInitialPointUsage().setExpireTime(calExpire.getTime());
        super.init();

    }

    @Test
    public void test_expirePointTask() {
        this.expirePointTask.expirePointTask();
    }
}
