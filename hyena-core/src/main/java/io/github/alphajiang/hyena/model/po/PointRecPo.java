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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.alphajiang.hyena.model.base.BasePo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointRecPo extends BasePo<Long>  {

    private long pid;
    private long seqNum;
    private BigDecimal total;
    private BigDecimal available;
    private BigDecimal used;
    private BigDecimal frozen;
    private BigDecimal refund;
    private BigDecimal cancelled;
    private BigDecimal expire;
    @Schema(name = "总成本")
    private BigDecimal totalCost;
    @Schema(name = "冻结的成本")
    private BigDecimal frozenCost;
    @Schema(name = "已消耗的成本")
    private BigDecimal usedCost;
    @Schema(name = "已退款的成本")
    private BigDecimal refundCost;
    private String tag;
    private String orderNo;

    private Integer sourceType;
    private Integer orderType;
    private Integer payType;
    private String extra;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "发放时间")
    private Date issueTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date expireTime;



}
