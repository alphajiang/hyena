package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.CostCalculator;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public void processPoint(PointUsage usage, PointCache pointCache) {
        //PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        PointPo curPoint = pointCache.getPoint();
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setCost(curPoint.getCost() - usage.getCost());
        var point2Update = new PointPo();
        point2Update.setCost(curPoint.getCost())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointLogDs.buildPointLog(PointOpType.REFUND, usage, curPoint);
        long gap = usage.getPoint();
        long cost = 0L;
        long sumPoint = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();

        var recLogsRet = this.loop(usage.getType(), pointCache, pointLog, gap);
        gap = gap - recLogsRet.getDeltaCost();
        cost = cost + recLogsRet.getDeltaCost();
        sumPoint += recLogsRet.getDelta();
        recLogs.addAll(recLogsRet.getRecLogs());
        log.debug("gap = {}", gap);

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
//        boolean ret = this.pointDs.update(usage.getType(), point2Update);
//        if (!ret) {
//            log.warn("refund cost failed!!! please retry later. usage = {}", usage);
//            return null;
//        }

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(getType(), usage, curPoint, pointLog, recLogs);
        //return pointCache.getPoint();
    }


    private LoopResult loop(String type, PointCache pointCache,
                            PointLogPo pointLog, long expected) {
        log.info("refund cost. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);

        LoopResult result = new LoopResult();
        long sum = 0L;
        long sumPoint = 0L;
        long cost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            long gap = expected - sum;
            long availableCost = this.costCalculator.getAvailableCost(rec);
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (availableCost < 1L) {
                // do nothing
            } else if (availableCost < gap) {
                sum += availableCost;
                long deltaCost = availableCost;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var rec4Update = this.pointRecDs.refundPoint(rec, delta, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = gap;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var rec4Update = this.pointRecDs.refundPoint(rec, delta, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointRecLogDs.buildRecLog(rec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
                break;
            }
        }
        pointCache.getPoint().setRecList(pointCache.getPoint().getRecList().stream().filter(rec -> rec.getEnable()).collect(Collectors.toList()));

        //var ret = point - sum;
        result.setDelta(sumPoint).setDeltaCost(cost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs);
        log.debug("result = {}", result);
        return result;
    }

}
