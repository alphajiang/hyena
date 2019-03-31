package com.aj.hyena.model.param;

import com.aj.hyena.model.base.BaseObject;
import com.aj.hyena.model.type.SortOrder;
import com.aj.hyena.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SortParam extends BaseObject {

    private List<String> columns = new ArrayList<>();
    private SortOrder order = SortOrder.desc;

    public static SortParam as(String column, SortOrder order) {
        return SortParam.buildSort(order).setColumns(List.of(column));
    }

    public static SortParam as(String col1, String col2, SortOrder order) {
        return SortParam.buildSort(order).setColumns(List.of(col1, col2));
    }

    public static SortParam as(String col1, String col2, String col3, SortOrder order) {
        return SortParam.buildSort(order).setColumns(List.of(col1, col2, col3));
    }

    private static SortParam buildSort(SortOrder order) {
        SortParam sort = new SortParam();
        sort.order = order;
        return sort;
    }

    public List<String> getColumns() {
        return columns;
    }

    public SortParam setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public String getColumnsString() {

        return CollectionUtils.join(this.columns, ",");

    }

    public SortOrder getOrder() {
        return order;
    }

    public SortParam setOrder(SortOrder order) {
        this.order = order;
        return this;
    }


}
