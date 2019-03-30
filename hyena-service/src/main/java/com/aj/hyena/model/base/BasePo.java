package com.aj.hyena.model.base;

import java.util.Date;

public abstract class BasePo extends BaseObject {

    private Boolean enable;
    private Date createTime;
    private Date updateTime;

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