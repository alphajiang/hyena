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

package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
public class PointCache {
    //private Long pid;
    private String key; // type + uid

    private PointVo point;


    private Date updateTime;

    private Semaphore lock = new Semaphore(1);

    public void lock() {
        try {
            this.lock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new HyenaServiceException("get lock failed", e);
        }
    }

    public void unlock() {
        this.lock.release();
    }


}
