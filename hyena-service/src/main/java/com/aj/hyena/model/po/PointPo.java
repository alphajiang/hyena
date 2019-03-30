package com.aj.hyena.model.po;

import com.aj.hyena.model.base.BasePo;

public class PointPo extends BasePo {

    private String cusId;
    private Long point;
    private Long available;
    private Long used;
    private Long frozen;
    private Long expire;


    public String getCusId() {
        return cusId;
    }

    public PointPo setCusId(String cusId) {
        this.cusId = cusId;
        return this;
    }

    public Long getPoint() {
        return point;
    }

    public PointPo setPoint(Long point) {
        this.point = point;
        return this;
    }

    public Long getAvailable() {
        return available;
    }

    public PointPo setAvailable(Long available) {
        this.available = available;
        return this;
    }

    public Long getUsed() {
        return used;
    }

    public PointPo setUsed(Long used) {
        this.used = used;
        return this;
    }

    public Long getFrozen() {
        return frozen;
    }

    public PointPo setFrozen(Long frozen) {
        this.frozen = frozen;
        return this;
    }

    public Long getExpire() {
        return expire;
    }

    public PointPo setExpire(Long expire) {
        this.expire = expire;
        return this;
    }
}
