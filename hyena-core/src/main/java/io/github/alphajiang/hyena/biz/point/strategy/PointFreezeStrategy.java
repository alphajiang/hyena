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
import io.github.alphajiang.hyena.biz.point.CostCalculator;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PointFreezeStrategy extends AbstractPointStrategy {

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


    @Autowired
    private CostCalculator costCalculator;


    @Override
    public CalcType getType() {
        return CalcType.FREEZE;
    }

    @Override
    public void processPoint(PointUsage usage, PointCache pointCache) {
        PointVo curPoint = pointCache.getPoint();
        if (curPoint.getAvailable() < usage.getPoint()) {
            log.warn("no enough available point. usage = {}, curPoint = {}", usage, curPoint);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        }

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setFrozen(curPoint.getFrozen() + usage.getPoint());
        PointPo point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable())
                .setFrozen(curPoint.getFrozen()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.FREEZE, usage, curPoint);
        long gap = usage.getPoint();
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();


        LoopResult recLogsRet = this.freezePointLoop(usage.getType(), pointCache,
                pointLog, gap);
        gap = gap - recLogsRet.getDelta();
        cost = cost + recLogsRet.getDeltaCost();
        recLogs.addAll(recLogsRet.getRecLogs());
        log.debug("gap = {}", gap);


        if (gap != 0L) {
            log.warn("no enough available point! gap = {}", gap);
        }
        if (cost > 0L) {
            pointLog.setDeltaCost(cost).setFrozenCost(pointLog.getFrozenCost() + cost);
            curPoint.setFrozenCost(curPoint.getFrozenCost() + cost);
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);

        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);

        pointCache.setUpdateTime(new Date());
        //return curPoint;
    }


    private LoopResult freezePointLoop(String type, PointCache pointCache,
                                       PointLogPo pointLog, long expected) {
        log.info("freeze. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);
//        ListPointRecParam param = new ListPointRecParam();
//        param.setUid(point.getUid()).setAvailable(true).setLock(true)
//                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
//                .setSize(5);
//        var recList = this.pointRecDs.listPointRec(type, param);
//        if (recList.isEmpty()) {
//            throw new HyenaNoPointException("no enough point", Level.DEBUG);
//        }
        LoopResult result = new LoopResult();
        long sum = 0L;
        long cost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getAvailable() < 1L) {
                // do nothing
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                long deltaCost = this.costCalculator.accountCost(rec, delta);
                cost += deltaCost;
                var rec4Update = this.pointRecDs.freezePoint2(rec, gap, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = this.costCalculator.accountCost(rec, gap);
                cost += deltaCost;
                var rec4Update = this.pointRecDs.freezePoint2(rec, gap, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, gap, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sum).setDeltaCost(cost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }


}
