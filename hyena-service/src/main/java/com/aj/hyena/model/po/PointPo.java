package com.aj.hyena.model.po;

import com.aj.hyena.model.base.BasePo;

public class PointPo extends BasePo {

    private String cusId;
    private Long point;

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
}
