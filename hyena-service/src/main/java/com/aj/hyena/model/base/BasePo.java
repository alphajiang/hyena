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

package com.aj.hyena.model.base;

import java.util.Date;

public abstract class BasePo extends BaseObject {

    private Long id;
    private Boolean enable;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public BasePo setId(Long id) {
        this.id = id;
        return this;
    }

    public Boolean getEnable() {
        return enable;
    }

    public BasePo setEnable(Boolean enable) {
        this.enable = enable;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public BasePo setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public BasePo setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}