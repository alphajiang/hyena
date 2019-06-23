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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.alphajiang.hyena.model.base.BasePo;
import io.github.alphajiang.hyena.utils.JacksonStringDeserialize;
import io.github.alphajiang.hyena.utils.JacksonStringSerialize;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class PointRecPo extends BasePo {

    private long pid;
    private Long total;
    private Long available;
    private Long used;
    private Long frozen;
    private Long cancelled;
    private Long expire;
    private String tag;

    private String extra;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(value = "发放时间")
    private Date issueTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date expireTime;

    public long getPid() {
        return pid;
    }

    public PointRecPo setPid(long pid) {
        this.pid = pid;
        return this;
    }

    public Long getTotal() {
        return total;
    }

    public PointRecPo setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Long getAvailable() {
        return available;
    }

    public PointRecPo setAvailable(Long available) {
        this.available = available;
        return this;
    }

    public Long getUsed() {
        return used;
    }

    public PointRecPo setUsed(Long used) {
        this.used = used;
        return this;
    }

    public Long getFrozen() {
        return frozen;
    }

    public PointRecPo setFrozen(Long frozen) {
        this.frozen = frozen;
        return this;
    }

    public Long getCancelled() {
        return cancelled;
    }

    public PointRecPo setCancelled(Long cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public Long getExpire() {
        return expire;
    }

    public PointRecPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public PointRecPo setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public PointRecPo setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
        return this;
    }

    @JsonSerialize(using = JacksonStringSerialize.class)
    public String getExtra() {
        return extra;
    }

    @JsonDeserialize(using = JacksonStringDeserialize.class)
    public PointRecPo setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public PointRecPo setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
        return this;
    }


}
