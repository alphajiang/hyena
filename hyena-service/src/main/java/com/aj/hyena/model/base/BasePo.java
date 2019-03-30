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