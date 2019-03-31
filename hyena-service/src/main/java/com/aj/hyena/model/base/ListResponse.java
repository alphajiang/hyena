package com.aj.hyena.model.base;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class ListResponse<T> extends BaseResponse {

    @ApiModelProperty(value = "总数据条数")
    private long total;

    @ApiModelProperty(value = "返回结果的数据部分")
    private List<T> data;

    public ListResponse() {
        data = new ArrayList<>();
        total = 0L;
    }

    public ListResponse(List<T> data) {
        this.data = data;
    }

    public ListResponse(List<T> data, long total) {
        this.total = total;
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
