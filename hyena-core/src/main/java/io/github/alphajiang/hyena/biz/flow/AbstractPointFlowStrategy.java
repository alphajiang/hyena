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
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

public abstract class AbstractPointFlowStrategy implements PointFlowStrategy {


    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowStrategyFactory pointFlowStrategyFactory;

    @PostConstruct
    public void init() {
        pointFlowStrategyFactory.addStrategy(this);
    }

    @Override
    public void addFlow2(PointUsage usage, PointLogPo pointLog, List<PointRecLogPo> recLogs) {
        this.pointLogDs.addPointLog(usage.getType(), pointLog);
        this.pointRecLogDs.addPointRecLogs(usage.getType(), recLogs);
    }
}
