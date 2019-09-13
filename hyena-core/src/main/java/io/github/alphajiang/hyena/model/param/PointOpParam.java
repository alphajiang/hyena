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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PointOpParam extends BaseObject {
    @ApiModelProperty(value = "请求消息序列号", example = "")
    private String seq;

    @ApiModelProperty(value = "积分类型", example = "score")
    private String type = "default";

    @ApiModelProperty(value = "用户ID", example = "customer_abc123")
    private String uid;

    @ApiModelProperty(value = "显示名称", example = "Tom")
    private String name;

    @ApiModelProperty(value = "积分数量", example = "1000")
    private long point;


    @ApiModelProperty(value = "标签", example = "")
    private String tag;

    @ApiModelProperty(value = "变动相关的订单", example = "")
    private String orderNo;


    @ApiModelProperty(value = "自定义来源")
    private Integer sourceType;
    @ApiModelProperty(value = "自定义订单类型")
    private Integer orderType;
    @ApiModelProperty(value = "自定义支付方式")
    private Integer payType;

    private Object extra;

    @ApiModelProperty(value = "备注", example = "this is a note")
    private String note = "";


}
