/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
