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

import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class PointVo extends PointPo {

    @Exclude
    private List<PointRecPo> recList;


    @Exclude
    private Map<String, FreezeOrderRecPo> forList;

    public synchronized void addForList(List<FreezeOrderRecPo> inForList) {
        if (CollectionUtils.isEmpty(inForList)) {
            return;
        }
        if (forList == null) {
            forList = new HashMap<>();
        }
        this.forList.putAll(inForList.stream().collect(Collectors.toMap(o -> o.getId(), o -> o, (l, r) -> l)));
    }

//    @Override
//    public String toString() {
//        return JsonUtils.toJsonString(this);
//    }
}
