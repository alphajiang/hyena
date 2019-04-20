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

public class PointRecLogPo extends BasePo {

    private long pid;
    private long recId;

    /**
     * 0: increase;
     * 1: decrease;
     * 2: freeze;
     * 3: unfreeze;
     * 4: expire
     */
    private Integer type;
    private Long delta;
    private Long available;
    private Long used;
    private Long frozen;
    private Long cancelled;
    private Long expire;
    private String note;

    public long getPid() {
        return pid;
    }

    public PointRecLogPo setPid(long pid) {
        this.pid = pid;
        return this;
    }

    public long getRecId() {
        return recId;
    }

    public PointRecLogPo setRecId(long recId) {
        this.recId = recId;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public PointRecLogPo setType(Integer type) {
        this.type = type;
        return this;
    }

    public Long getDelta() {
        return delta;
    }

    public PointRecLogPo setDelta(Long delta) {
        this.delta = delta;
        return this;
    }

    public Long getAvailable() {
        return available;
    }

    public PointRecLogPo setAvailable(Long available) {
        this.available = available;
        return this;
    }

    public Long getUsed() {
        return used;
    }

    public PointRecLogPo setUsed(Long used) {
        this.used = used;
        return this;
    }

    public Long getFrozen() {
        return frozen;
    }

    public PointRecLogPo setFrozen(Long frozen) {
        this.frozen = frozen;
        return this;
    }

    public Long getCancelled() {
        return cancelled;
    }

    public PointRecLogPo setCancelled(Long cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public Long getExpire() {
        return expire;
    }

    public PointRecLogPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }


    public String getNote() {
        return note;
    }

    public PointRecLogPo setNote(String note) {
        this.note = note;
        return this;
    }
}
