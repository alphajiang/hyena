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
