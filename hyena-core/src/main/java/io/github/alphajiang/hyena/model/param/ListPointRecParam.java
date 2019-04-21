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

package io.github.alphajiang.hyena.model.param;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class ListPointRecParam extends BaseListParam {


    @ApiModelProperty(value = "用户ID")
    private String uid;

    private boolean available = false;
    private Boolean frozen;
    private Date expireTime;

    public String getUid() {
        return uid;
    }

    public ListPointRecParam setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public boolean getAvailable() {
        return available;
    }

    public ListPointRecParam setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public ListPointRecParam setFrozen(Boolean frozen) {
        this.frozen = frozen;
        return this;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public ListPointRecParam setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
        return this;
    }
}
