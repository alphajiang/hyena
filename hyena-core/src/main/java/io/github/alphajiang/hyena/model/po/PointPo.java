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

package io.github.alphajiang.hyena.model.po;

import io.github.alphajiang.hyena.model.base.BasePo;

public class PointPo extends BasePo {

    private String uid;
    private Long point;
    private Long available;
    private Long used;
    private Long frozen;
    private Long expire;

    public String getUid() {
        return uid;
    }

    public PointPo setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public Long getPoint() {
        return point;
    }

    public PointPo setPoint(Long point) {
        this.point = point;
        return this;
    }

    public Long getAvailable() {
        return available;
    }

    public PointPo setAvailable(Long available) {
        this.available = available;
        return this;
    }

    public Long getUsed() {
        return used;
    }

    public PointPo setUsed(Long used) {
        this.used = used;
        return this;
    }

    public Long getFrozen() {
        return frozen;
    }

    public PointPo setFrozen(Long frozen) {
        this.frozen = frozen;
        return this;
    }

    public Long getExpire() {
        return expire;
    }

    public PointPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }
}
