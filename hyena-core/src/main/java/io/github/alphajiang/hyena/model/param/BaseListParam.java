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

package io.github.alphajiang.hyena.model.param;

import io.github.alphajiang.hyena.model.base.BaseObject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class BaseListParam extends BaseObject {

    private String type;

    @Schema(name = "搜索关键词")
    private String sk = "";

    @Schema(name = "是否有效")
    private Boolean enable;

    private boolean lock = false;


    @Schema(name = "分页开始")
    private Long start;

    @Schema(name = "查询最大数量")
    private Integer size;

    @Schema(name = "排序方式")
    private List<SortParam> sorts;


    public String getType() {
        return type;
    }

    public BaseListParam setType(String type) {
        this.type = type;
        return this;
    }

    public String getSk() {
        return sk;
    }

    public BaseListParam setSk(String sk) {
        this.sk = sk;
        return this;
    }

    public Boolean getEnable() {
        return enable;
    }

    public BaseListParam setEnable(Boolean enable) {
        this.enable = enable;
        return this;
    }

    public boolean isLock() {
        return lock;
    }

    public BaseListParam setLock(boolean lock) {
        this.lock = lock;
        return this;
    }

    public Long getStart() {
        return start;
    }

    public BaseListParam setStart(Long start) {
        this.start = start;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public BaseListParam setSize(Integer size) {
        this.size = size;
        return this;
    }

    public List<SortParam> getSorts() {
        return sorts;
    }

    public BaseListParam setSorts(List<SortParam> sorts) {
        this.sorts = sorts;
        return this;
    }
}
