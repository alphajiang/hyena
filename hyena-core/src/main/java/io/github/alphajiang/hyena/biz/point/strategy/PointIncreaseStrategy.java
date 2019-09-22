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

package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 增加积分
 */
@Component
public class PointIncreaseStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointIncreaseStrategy.class);

    @Autowired
    private PointDs pointDs;


    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.INCREASE;
    }

    @Override
    @Transactional //(isolation = Isolation.READ_COMMITTED)
    public PointPo process(PointUsage usage) {
        logger.info("increase. usage = {}", usage);
        super.preProcess(usage);
        var point2Update = new PointPo();
        point2Update.setPoint(usage.getPoint())
                .setAvailable(usage.getPoint())
                .setUid(usage.getUid());
        if (usage.getCost() != null && usage.getCost() > 0L) {
            point2Update.setCost(usage.getCost());
        } else {
            point2Update.setCost(0L);
        }
        if (StringUtils.isNotBlank(usage.getName())) {
            point2Update.setName(usage.getName());
        }

        this.pointDs.addPoint(usage.getType(), point2Update);
        PointPo retPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);

        if (usage.getPoint() > 0L) {
            var pointRec = this.pointRecDs.addPointRec(usage, retPoint.getId(), retPoint.getSeqNum());

            if(usage.getPoint() > retPoint.getPoint()) {
                // 之前有欠款 TODO: 待验证
                long number = usage.getPoint() - retPoint.getPoint();
                pointRec.setAvailable(pointRec.getAvailable() - number);
                pointRec.setUsed(number);
                this.pointRecDs.updatePointRec(usage.getType(), pointRec);
            }

            PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.INCREASE, usage, retPoint);
            PointRecLogPo recLog = this.pointRecLogDs.buildRecLog(pointRec, pointLog, usage.getPoint(),
                    usage.getCost());


            pointFlowService.addFlow(getType(), usage, retPoint, pointLog, List.of(recLog));
        }

        return retPoint;
    }
}
