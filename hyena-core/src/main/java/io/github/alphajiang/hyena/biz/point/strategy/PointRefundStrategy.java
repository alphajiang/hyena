package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.calculator.CostCalculator;
import io.github.alphajiang.hyena.biz.calculator.PointRecCalculator;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.ds.service.FreezeOrderRecDs;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PointRefundStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private FreezeOrderRecDs freezeOrderRecDs;

    @Autowired
    private PointUnfreezeStrategy pointUnfreezeStrategy;

    @Autowired
    private PointRecCalculator pointRecCalculator;

    @Autowired
    private PointFlowService pointFlowService;

    @Autowired
    private CostCalculator costCalculator;

    @Autowired
    private PointBuilder pointBuilder;

    @Override
    public CalcType getType() {
        return CalcType.REFUND;
    }


    @Override
    @Transactional
    public PointOpResult process(PointUsage usage) {
        log.info("refund. usage = {}", usage);

        try (PointWrapper pw = preProcess(usage, true, true)) {
            PointCache p = pw.getPointCache();

            if (usage.getUnfreezePoint() != null && usage.getUnfreezePoint() > 0L) {
                List<FreezeOrderRecPo> forList = this.freezeOrderRecDs.getFreezeOrderRecList(usage.getType(),
                        p.getPoint().getId(),
                        usage.getOrderType(), usage.getOrderNo());


                PointUsage usage4Unfreeze = new PointUsage();
                BeanUtils.copyProperties(usage, usage4Unfreeze);
                usage4Unfreeze.setPoint(usage.getUnfreezePoint());

                this.pointUnfreezeStrategy.process(usage4Unfreeze);

                return this.processPoint(usage, p, forList);
            } else {
                return this.processPoint(usage, p);
            }


        } catch (Exception e) {
            throw e;
        }

    }


    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        log.info(">> pointCache = {}", pointCache);
        //PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(usage.getCost(), "invalid parameter: cost");
        PointPo curPoint = pointCache.getPoint();
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getCost(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);
        var recLogsRet = this.loop(usage.getType(), pointCache, pointLog, usage.getCost());

        curPoint.setPoint(curPoint.getPoint() - recLogsRet.getDelta())
                .setAvailable(curPoint.getAvailable() - recLogsRet.getDelta())
                .setRefund(curPoint.getRefund() + recLogsRet.getDelta())
                .setCost(curPoint.getCost() - recLogsRet.getDeltaCost());
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setRefund(curPoint.getRefund())
                .setCost(curPoint.getCost());

        pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);
        pointLog.setDelta(recLogsRet.getDelta())
                .setDeltaCost(recLogsRet.getDeltaCost());

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, recLogsRet.getRecLogs());

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        log.info("<< pointCache = {}", pointCache);
        return ret;


    }

    public PointOpResult processPoint(PointUsage usage, PointCache pointCache, List<FreezeOrderRecPo> forList) {
        log.info(">> pointCache = {}", pointCache);

        PointPo curPoint = pointCache.getPoint();
        long availableCost = curPoint.getCost().longValue() - curPoint.getFrozenCost().longValue();
        HyenaAssert.isTrue(availableCost >= usage.getPoint(),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);

        LoopResult recLogsRet = this.refundLoop(usage, pointCache, pointLog, forList);


        curPoint.setPoint(curPoint.getPoint() - recLogsRet.getDelta())
                .setAvailable(curPoint.getAvailable() - recLogsRet.getDelta())
                .setRefund(curPoint.getRefund() + recLogsRet.getDelta())
                .setCost(curPoint.getCost() - recLogsRet.getDeltaCost());
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setRefund(curPoint.getRefund())
                .setCost(curPoint.getCost());

        pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);
        pointLog.setDelta(recLogsRet.getDelta())
                .setDeltaCost(recLogsRet.getDeltaCost());

        pointFlowService.updatePoint(usage.getType(), point2Update);
        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFlow(usage, pointLog, recLogsRet.getRecLogs());
        //return pointCache.getPoint();

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        log.info("<< pointCache = {}", pointCache);
        return ret;
    }


    private LoopResult loop(String type, PointCache pointCache,
                            PointLogPo pointLog, long expected) {
        log.info("refund cost. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);

        LoopResult result = new LoopResult();
        long sum = 0L;
        long sumPoint = 0L;
        long cost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
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
                var rec4Update = this.pointRecCalculator.refundPoint(rec, delta, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum += gap;
                long deltaCost = gap;
                long delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint += delta;
                cost += deltaCost;
                var rec4Update = this.pointRecCalculator.refundPoint(rec, delta, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, deltaCost);
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

    private LoopResult refundLoop(PointUsage usage, PointCache pointCache,
                                  PointLogPo pointLog, List<FreezeOrderRecPo> forList) {
        log.info("refund. type = {}, uid = {}",
                usage.getType(), pointCache.getPoint().getUid());
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();

        // 将同一个积分块的冻结记录做合并
        Map<Long, FreezeOrderRecPo> forMap = new HashMap<>();
        forList.stream().forEach(fo -> {
            if (forMap.containsKey(fo.getRecId())) {
                FreezeOrderRecPo l = forMap.get(fo.getRecId());
                l.setFrozen(l.getFrozen() + fo.getFrozen())
                        .setFrozenCost(l.getFrozenCost() + fo.getFrozenCost());
            } else {
                FreezeOrderRecPo l = new FreezeOrderRecPo();
                BeanUtils.copyProperties(fo, l);
                forMap.put(fo.getRecId(), l);
            }
        });

        forMap.forEach((k, f) -> {
            pointCache.getPoint().getRecList()
                    .stream().filter(rec -> rec.getId() == f.getRecId())
                    .findFirst()
                    .ifPresent(rec -> {
                        PointRecPo recRet = this.pointRecCalculator.refundPoint(rec, f.getFrozen(), f.getFrozenCost());
                        recList4Update.add(recRet);
                        var recLog = this.pointBuilder.buildRecLog(rec, pointLog, f.getFrozen(), f.getFrozenCost());
                        recLogs.add(recLog);
                    });
        });
        long delta = forList.stream().mapToLong(FreezeOrderRecPo::getFrozen).sum();
        long deltaCost = forList.stream().mapToLong(FreezeOrderRecPo::getFrozenCost).sum();
        LoopResult result = new LoopResult();

        result.setDelta(delta)
                .setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs)
                .setForList(forList);
        log.debug("result = {}", result);
        return result;
    }
}
