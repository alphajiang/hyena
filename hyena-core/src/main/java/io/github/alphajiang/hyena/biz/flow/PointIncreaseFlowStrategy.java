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

package io.github.alphajiang.hyena.biz.flow;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointIncreaseFlowStrategy  extends AbstractPointFlowStrategy{

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;




    @Override
    public CalcType getType() {
        return CalcType.INCREASE;
    }

    @Override
    @Transactional
    public void addFlow(PointUsage usage, PointPo point) {
        var pointRec = this.pointRecDs.addPointRec(usage, point.getId());
        var recLog = this.pointRecLogDs.addLogByRec(usage.getType(), PointStatus.INCREASE,
                pointRec, usage.getPoint(), usage.getNote());
        var recLogs = List.of(recLog);
        this.pointLogDs.addPointLog(usage.getType(), point, usage.getPoint(),
                usage.getTag(), usage.getExtra(), recLogs);
    }
}
