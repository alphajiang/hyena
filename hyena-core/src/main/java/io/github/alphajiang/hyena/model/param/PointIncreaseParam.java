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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointIncreaseParam extends PointOpParam {


    @Nullable
    @Schema(name = "实际成本", example = "1.00")
    private BigDecimal cost;

    @Nullable
    @Schema(name = "获取积分时间", example = "2005-12-25 15:34:46")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date issueTime;

    @Nullable
    @Schema(name = "过期时间", example = "2025-10-24 15:34:46")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date expireTime;


//    @Nullable
//    public Long getCost() {
//        return cost;
//    }
//
//    public void setCost(@Nullable Long cost) {
//        this.cost = cost;
//    }
//
//    @Nullable
//    public Date getIssueTime() {
//        return issueTime;
//    }
//
//    public PointIncreaseParam setIssueTime(@Nullable Date issueTime) {
//        this.issueTime = issueTime;
//        return this;
//    }
//
//    @Nullable
//    public Date getExpireTime() {
//        return expireTime;
//    }
//
//    public void setExpireTime(@Nullable Date expireTime) {
//        this.expireTime = expireTime;
//    }
}
