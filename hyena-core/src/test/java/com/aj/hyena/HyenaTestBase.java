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

package com.aj.hyena;

import com.aj.hyena.ds.service.PointTableService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HyenaTestMain.class)
@Transactional
public abstract class HyenaTestBase {

    @Autowired
    private PointTableService pointTableService;

    private String pointType;

    public HyenaTestBase() {
        this.pointType = UUID.randomUUID().toString().substring(0, 6);
    }


    public void init() {
        pointTableService.getOrCreateTable(this.pointType);
    }

    public String getPointType() {
        return this.pointType;
    }
}
