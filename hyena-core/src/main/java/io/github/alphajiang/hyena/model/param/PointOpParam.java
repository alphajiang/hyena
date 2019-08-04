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

import io.github.alphajiang.hyena.model.base.BaseObject;
import io.swagger.annotations.ApiModelProperty;

public class PointOpParam extends BaseObject {
    @ApiModelProperty(value = "请求消息序列号", example = "")
    private String seq;

    @ApiModelProperty(value = "积分类型", example = "score")
    private String type = "default";

    @ApiModelProperty(value = "用户ID", example = "customer_abc123")
    private String uid;

    @ApiModelProperty(value = "显示名称", example = "Tom")
    private String name;

    @ApiModelProperty(value = "积分数量", example = "1000")
    private long point;


    @ApiModelProperty(value = "标签", example = "")
    private String tag;


    private Object extra;

    @ApiModelProperty(value = "备注", example = "this is a note")
    private String note = "";

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPoint() {
        return point;
    }

    public void setPoint(long point) {
        this.point = point;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
