package com.aj.hyena.model.po;

import com.aj.hyena.model.base.BasePo;

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
    private Long expire;
    private String tag;
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

    public Long getExpire() {
        return expire;
    }

    public PointRecLogPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public PointRecLogPo setTag(String tag) {
        this.tag = tag;
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
