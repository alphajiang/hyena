package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.CostCalculator;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PointRefundStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Autowired
    private CostCalculator costCalculator;

    @Override
    public CalcType getType() {
        return CalcType.REFUND;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        log.info("refund.  usage = {}", usage);
        super.preProcess(usage);
        PointPo ret = this.refundCost(usage);
        return ret;
    }

    private PointPo refundCost(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setCost(curPoint.getCost() - usage.getCost());
        var point2Update = new PointPo();
        point2Update.setCost(curPoint.getCost())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        curPoint.setSeqNum(curPoint.getSeqNum() + 1);

        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.REFUND, usage, curPoint);
        long gap = usage.getPoint();
        long cost = 0L;
        long sumPoint = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        try {
            do {
                var recLogsRet = this.loop(usage.getType(), curPoint, pointLog, gap);
                gap = gap - recLogsRet.getDeltaCost();
                cost = cost + recLogsRet.getDeltaCost();
                sumPoint += recLogsRet.getDelta();
                recLogs.addAll(recLogsRet.getRecLogs());
                log.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        if (gap != 0L) {
            log.warn("no enough available cost! gap = {}", gap);
            //throw new HyenaServiceException("no enough available point!");
        }
        curPoint.setAvailable(curPoint.getAvailable() - sumPoint)
                .setRefund(curPoint.getRefund() + sumPoint);
        point2Update.setAvailable(curPoint.getAvailable())
                .setRefund(curPoint.getRefund());

        pointLog.setDelta(sumPoint)
                .setDeltaCost(cost).setRefund(curPoint.getRefund())
                .setAvailable(curPoint.getAvailable());
        //HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");
        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if (!ret) {
            log.warn("refund cost failed!!! please retry later. usage = {}", usage);
            return null;
        }

        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        return curPoint;
    }


    private LoopResult loop(String type, PointPo point, PointLogPo pointLog, long expected) {
        log.info("refund cost. type = {}, uid = {}, expected = {}", type, point.getUid(), expected);
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(point.getUid()).setCost(true).setAvailable(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecDs.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        LoopResult result = new LoopResult();
        long sum = 0L;
        long sumPoint = 0L;
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : recList) {
            long gap = expected - sum;
            long availableCost = this.costCalculator.getAvailableCost(rec);
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (availableCost < gap) {
                sum += availableCost;
                long deltaCost = availableCost;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var retRec = this.pointRecDs.refundPoint(type, rec, delta, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = gap;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var retRec = this.pointRecDs.refundPoint(type, rec, delta, deltaCost);
                var recLog = this.pointRecLogDs.buildRecLog(retRec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }
        //var ret = point - sum;
        result.setDelta(sumPoint).setDeltaCost(cost).setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }

}
