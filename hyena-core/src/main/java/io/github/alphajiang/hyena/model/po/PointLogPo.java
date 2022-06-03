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

package io.github.alphajiang.hyena.model.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.alphajiang.hyena.model.base.BasePo;
import io.github.alphajiang.hyena.utils.JacksonStringDeserialize;
import io.github.alphajiang.hyena.utils.JacksonStringSerialize;
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
public class PointLogPo extends BasePo<Long> {

    private long pid;
    private String uid;
    private String subUid;
    private long seqNum;
    private BigDecimal delta;
    @Schema(title = "变动部分的实际成本")
    private BigDecimal deltaCost;
    private BigDecimal point;
    private BigDecimal available;
    private BigDecimal used;
    private BigDecimal frozen;
    private BigDecimal refund;
    private BigDecimal expire;
    @Schema(title = "变动后,实际成本")
    private BigDecimal cost;
    @Schema(title = "变动后,冻结的成本")
    private BigDecimal frozenCost;
    /**
     * PointOpType
     */
    private Integer type;
    private String tag;
    private String orderNo;

    private Integer sourceType;
    private Integer orderType;
    private Integer payType;
    @JsonSerialize(using = JacksonStringSerialize.class)
    @JsonDeserialize(using = JacksonStringDeserialize.class)
    private String extra;
    private String note;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean abnormal;

}
