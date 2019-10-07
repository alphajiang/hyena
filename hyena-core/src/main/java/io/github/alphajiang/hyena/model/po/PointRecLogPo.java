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

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointRecLogPo extends BasePo {

    private String uid;
    private long pid;
    private long seqNum;
    private long recId;

    /**
     * PointOpType
     */
    private Integer type;
    private Long delta;
    @ApiModelProperty("变动部分的实际成本")
    private Long deltaCost;
    private Long available;
    private Long used;
    private Long frozen;
    private Long refund;
    private Long cancelled;
    private Long expire;
    @ApiModelProperty("变动后,实际成本")
    private Long cost;
    @ApiModelProperty("变动后,冻结的实际成本")
    private Long frozenCost;
    @ApiModelProperty("变动后,已消耗的实际成本")
    private Long usedCost;
    @ApiModelProperty("已退款的成本")
    private Long refundCost;

    private Integer sourceType;
    private Integer orderType;
    private Integer payType;
    private String note;


}
