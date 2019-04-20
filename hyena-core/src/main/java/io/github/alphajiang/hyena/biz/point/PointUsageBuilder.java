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

import io.github.alphajiang.hyena.model.param.PointCancelParam;
import io.github.alphajiang.hyena.model.param.PointIncreaseParam;
import io.github.alphajiang.hyena.model.param.PointOpParam;

public class PointUsageBuilder {

    public static PointUsage fromPointOpParam(PointOpParam param) {
        PointUsage usage = new PointUsage();
        usage.setType(param.getType()).setUid(param.getUid()).setPoint(param.getPoint())
                .setNote(param.getNote());
        return usage;
    }

    public static PointUsage fromPointIncreaseParam(PointIncreaseParam param) {
        PointUsage usage = new PointUsage();
        usage.setType(param.getType()).setUid(param.getUid()).setPoint(param.getPoint())
                .setTag(param.getTag())
                .setNote(param.getNote()).setExpireTime(param.getExpireTime());
        return usage;
    }

    public static PointUsage fromPointCancelParam(PointCancelParam param) {
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setRecId(param.getRecId());
        return usage;
    }

}
