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

import io.github.alphajiang.hyena.model.base.BasePo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PointPo extends BasePo {

    private String uid;
    private String subUid;
    private String name;
    private BigDecimal point;
    private BigDecimal available;
    private BigDecimal used;
    private BigDecimal frozen;
    private BigDecimal refund;
    private BigDecimal expire;
    @ApiModelProperty("实际成本(含冻结部分)")
    private BigDecimal cost;
    @ApiModelProperty("冻结的成本")
    private BigDecimal frozenCost;
    private Long seqNum;

}
