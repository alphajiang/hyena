package com.aj.hyena.model.po;

import com.aj.hyena.model.base.BasePo;

import java.util.Date;

public class PointRecPo extends BasePo {

    private long pid;
    private Long total;
    private Long available;
    private Long used;
    private Long frozen;
    private Long expire;
    private String tag;
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

    public Date getExpireTime() {
        return expireTime;
    }

    public PointRecPo setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
        return this;
    }
}
