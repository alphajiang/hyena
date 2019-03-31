package com.aj.hyena.model.param;

public class ListPointRecParam extends BaseListParam {

    private String cusId;
    private boolean available = false;
    private boolean frozen = false;

    public String getCusId() {
        return cusId;
    }

    public ListPointRecParam setCusId(String cusId) {
        this.cusId = cusId;
        return this;
    }

    public boolean getAvailable() {
        return available;
    }

    public ListPointRecParam setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public boolean getFrozen() {
        return frozen;
    }

    public ListPointRecParam setFrozen(boolean frozen) {
        this.frozen = frozen;
        return this;
    }
}
