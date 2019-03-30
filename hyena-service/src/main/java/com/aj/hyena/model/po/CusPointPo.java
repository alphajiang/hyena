package com.aj.hyena.model.po;

import com.aj.hyena.model.base.BasePo;

public class CusPointPo extends BasePo {

    private String cusId;
    private Long point;

    public String getCusId() {
        return cusId;
    }

    public CusPointPo setCusId(String cusId) {
        this.cusId = cusId;
        return this;
    }

    public Long getPoint() {
        return point;
    }

    public CusPointPo setPoint(Long point) {
        this.point = point;
        return this;
    }
}
