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

package io.github.alphajiang.hyena.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
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
public class PointRecLogDto extends PointRecLogPo {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String uid;

    private BigDecimal total;


    @Schema(title = "变动记录的订单号")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String orderNo;

    @Schema(title = "创建积分块的订单号")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String recOrigOrderNo;
}
