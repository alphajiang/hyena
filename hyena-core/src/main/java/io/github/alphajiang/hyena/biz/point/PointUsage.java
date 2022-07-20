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

package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.base.BaseObject;
import io.github.alphajiang.hyena.utils.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PointUsage extends BaseObject {
    private String type;
    private String uid;
    private String subUid;
    private String name;
    private BigDecimal point;
    private BigDecimal unfreezePoint; // 消费积分时同时解冻的积分数量
    private BigDecimal cost;
    private Boolean unfreezeByOrderNo;
    private Long recId; // 积分记录的ID
    private String recOrderNo;  // 创建积分记录对应的订单号
    private String tag;
    private String orderNo;

    private Integer sourceType;
    private Integer orderType;
    private Integer payType;
    private String extra;
    private String note;
    private Date issueTime;
    private Date expireTime;

    private boolean doUpdate = true;

//    private PointWrapper pw;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
