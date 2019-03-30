package com.aj.hyena.model.base;

public class ObjectResponse<T> extends BaseResponse {
    protected T data;

    public ObjectResponse() {

    }

    public ObjectResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
