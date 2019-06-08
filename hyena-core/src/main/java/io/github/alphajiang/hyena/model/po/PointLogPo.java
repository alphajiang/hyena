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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.alphajiang.hyena.model.base.BasePo;
import io.github.alphajiang.hyena.utils.JacksonStringDeserialize;
import io.github.alphajiang.hyena.utils.JacksonStringSerialize;

public class PointLogPo extends BasePo {

    private String uid;
    private String recLogIds;
    private Long delta;
    private Long point;
    private Long available;
    private Long used;
    private Long frozen;
    private Long expire;
    /**
     * PointStatus
     */
    private Integer type;
    private String tag;
    private String extra;
    private String note;

    public String getUid() {
        return uid;
    }

    public PointLogPo setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getRecLogIds() {
        return recLogIds;
    }

    public PointLogPo setRecLogIds(String recLogIds) {
        this.recLogIds = recLogIds;
        return this;
    }

    public Long getDelta() {
        return delta;
    }

    public PointLogPo setDelta(Long delta) {
        this.delta = delta;
        return this;
    }

    public Long getPoint() {
        return point;
    }

    public PointLogPo setPoint(Long point) {
        this.point = point;
        return this;
    }

    public Long getAvailable() {
        return available;
    }

    public PointLogPo setAvailable(Long available) {
        this.available = available;
        return this;
    }

    public Long getUsed() {
        return used;
    }

    public PointLogPo setUsed(Long used) {
        this.used = used;
        return this;
    }

    public Long getFrozen() {
        return frozen;
    }

    public PointLogPo setFrozen(Long frozen) {
        this.frozen = frozen;
        return this;
    }

    public Long getExpire() {
        return expire;
    }

    public PointLogPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public PointLogPo setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public PointLogPo setTag(String tag) {
        this.tag = tag;
        return this;
    }

    @JsonSerialize(using = JacksonStringSerialize.class)
    public String getExtra() {
        return extra;
    }

    @JsonDeserialize(using = JacksonStringDeserialize.class)
    public PointLogPo setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public String getNote() {
        return note;
    }

    public PointLogPo setNote(String note) {
        this.note = note;
        return this;
    }
}
