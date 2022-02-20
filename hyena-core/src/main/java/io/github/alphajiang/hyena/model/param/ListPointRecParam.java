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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListPointRecParam extends BaseListParam {

    @Schema(name = "积分记录ID")
    private List<Long> recIdList;

    @Schema(name = "用户ID")
    private String uid;

    @Schema(name = "用户二级ID")
    private String subUid;

    @Schema(name = "标签")
    private String tag;

    @Schema(name = "创建积分的序列号列表")
    private List<Long> seqNumList;

    @Schema(name = "是否获取积分块对应的流水明细", example = "false")
    private Boolean fetchRecLogs;

    //private boolean available = false;
    private Boolean cost;
    private Boolean frozen;
    private Date expireTime;


}
