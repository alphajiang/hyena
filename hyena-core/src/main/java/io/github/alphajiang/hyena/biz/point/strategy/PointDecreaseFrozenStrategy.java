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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointLogService;
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.ds.service.PointService;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class PointDecreaseFrozenStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointDecreaseFrozenStrategy.class);

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRecService pointRecService;

    @Autowired
    private PointLogService pointLogService;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE_FROZEN;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        logger.info("decrease frozen. usage = {}", usage);
        super.preProcess(usage);
        PointPo curPoint = this.pointService.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getFrozen(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");

        long gap = usage.getPoint();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.decreasePointUnfreezeLoop(usage.getType(), usage.getUid(), gap, usage.getNote());
                gap = gap - recLogsRet.stream().mapToLong(PointRecLogPo::getDelta).sum();
                recLogs.addAll(recLogsRet);
                logger.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        HyenaAssert.isTrue(gap == 0L, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point!");
        curPoint.setPoint(curPoint.getPoint() - usage.getPoint())
                .setFrozen(curPoint.getFrozen() - usage.getPoint())
                .setUsed(curPoint.getUsed() + usage.getPoint());

        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint()).setFrozen(curPoint.getFrozen())
                .setUsed(curPoint.getUsed()).setId(curPoint.getId());
        this.pointService.update(usage.getType(), point2Update);

        var cusPoint = this.pointService.getCusPoint(usage.getType(), usage.getUid(), false);
        this.pointLogService.addPointLog(usage.getType(), cusPoint, usage.getPoint(),
                usage.getTag(), usage.getExtra(), recLogs);




        return curPoint;
    }

    private List<PointRecLogPo> decreasePointUnfreezeLoop(String type, String uid, long point, String note) {
        logger.info("decrease unfreeze. type = {}, uid = {}, point = {}", type, uid, point);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(uid).setFrozen(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecService.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        long sum = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = point - sum;
            if (gap < 1L) {
                logger.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen() < gap) {
                sum += rec.getFrozen();

                var recLog = this.pointRecService.decreasePointUnfreeze(type, rec, gap, note);
                recLogs.add(recLog);
            } else {
                sum += gap;
                var recLog = this.pointRecService.decreasePointUnfreeze(type, rec, gap, note);
                recLogs.add(recLog);
                break;
            }
        }

        //var ret = point - sum;
        logger.debug("sum = {}", sum);
        return recLogs;
    }
}
