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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointOpParam extends BaseObject {
    @Schema(title = "请求消息序列号", example = "", hidden = true)
    private String seq;

    @Schema(title = "积分类型", example = "score")
    private String type = "default";

    @Schema(title = "用户ID", example = "customer_abc123")
    private String uid;

    @Schema(title = "用户二级ID", example = "customer_abc123")
    private String subUid;

    @Schema(title = "显示名称", example = "Tom")
    private String name;

    @Schema(title = "积分数量", example = "10.00")
    private BigDecimal point;

    @Schema(title = "指定操作的积分块ID", example = "123")
    private Long recId;

    @Schema(title = "标签", example = "")
    private String tag;

    @Schema(title = "变动相关的订单", example = "")
    private String orderNo;


    @Schema(title = "自定义来源")
    private Integer sourceType;
    @Schema(title = "自定义订单类型")
    private Integer orderType;
    @Schema(title = "自定义支付方式")
    private Integer payType;

    private Object extra;

    @Schema(title = "备注", example = "this is a note")
    private String note = "";


}
