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

package io.github.alphajiang.hyena.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PointOpResult extends PointPo {

    @Schema(title = "变更的积分", example = "100.00")
    private BigDecimal opPoint;

    @Schema(title = "变更的成本", example = "10.00")
    private BigDecimal opCost;

    @Schema(title = "账户变更明细")
    private List<PointLogDto> logs;

    @Schema(title = "积分块变更明细")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PointRecLogDto> recLogList;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonIgnore
    private UpdateQueue updateQ = new UpdateQueue();

    @Data
    @Accessors(chain = true)
    public static class UpdateQueue {
        private PointPo point = new PointPo();
        private List<PointLogPo> logs = new ArrayList<>();
        private List<PointRecPo> recList = new ArrayList<>();
        private List<FreezeOrderRecPo> foList = new ArrayList<>();
        private List<PointRecLogDto> recLogs = new ArrayList<>();
    }


    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
