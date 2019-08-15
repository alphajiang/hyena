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
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PointDecreaseFrozenFlowStrategy  extends AbstractPointFlowStrategy {

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointUnfreezeFlowStrategy pointUnfreezeFlowStrategy;

    @Autowired
    private PointDecreaseFlowStrategy pointDecreaseFlowStrategy;

    @Override
    public CalcType getType() {
        return CalcType.DECREASE_FROZEN;
    }

    @Override
    @Transactional
    public void addFlow(PointUsage usage, PointPo point) {
        if(usage.getUnfreezePoint()!= null && usage.getUnfreezePoint() > 0L) {
            // 有要解冻的积分, 先做解冻
            PointUsage usage4Unfreeze = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Unfreeze);
            usage4Unfreeze.setPoint(usage.getUnfreezePoint());
            pointUnfreezeFlowStrategy.addFlow(usage4Unfreeze, point);
        }

        long gap = usage.getPoint();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.decreasePointUnfreezeLoop(usage.getType(), usage.getUid(), point,
                        gap, usage.getNote());
                gap = gap - recLogsRet.stream().mapToLong(PointRecLogPo::getDelta).sum();
                recLogs.addAll(recLogsRet);
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if(gap > 0L) {
            // 超扣部分
            PointUsage usage4Decrease = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Decrease);
            usage4Decrease.setPoint(gap);
            pointDecreaseFlowStrategy.addFlow(usage4Decrease, point);
        }
//        HyenaAssert.isTrue(gap == 0L, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
//                "no enough frozen point!");

        this.pointLogDs.addPointLog(usage.getType(), point, usage.getPoint(),
                usage.getTag(), usage.getOrderNo(), usage.getExtra(), recLogs);
    }


    private List<PointRecLogPo> decreasePointUnfreezeLoop(String type, String uid, PointPo point, long expected, String note) {
        log.info("decrease unfreeze. type = {}, uid = {}, expected = {}", type, uid, expected);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(uid).setFrozen(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecDs.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        long sum = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen() < gap) {
                sum += rec.getFrozen();

                var recLog = this.pointRecDs.decreasePointUnfreeze(type, rec,point.getSeqNum(),  gap, note);
                recLogs.add(recLog);
            } else {
                sum += gap;
                var recLog = this.pointRecDs.decreasePointUnfreeze(type, rec, point.getSeqNum(), gap, note);
                recLogs.add(recLog);
                break;
            }
        }

        //var ret = point - sum;
        log.debug("sum = {}", sum);
        return recLogs;
    }
}
