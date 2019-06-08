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

public class ListPointRecLogParam extends BaseListParam {


    @ApiModelProperty(value = "用户ID")
    private String uid;

    @ApiModelProperty(value = "用户记录ID")
    private long pid;

    @ApiModelProperty(value = "积分记录ID")
    private long recId;


    @ApiModelProperty(value = "标签")
    private String tag;



    public String getUid() {
        return uid;
    }

    public ListPointRecLogParam setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public long getPid() {
        return pid;
    }

    public ListPointRecLogParam setPid(long pid) {
        this.pid = pid;
        return this;
    }

    public long getRecId() {
        return recId;
    }

    public ListPointRecLogParam setRecId(long recId) {
        this.recId = recId;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public ListPointRecLogParam setTag(String tag) {
        this.tag = tag;
        return this;
    }


}
