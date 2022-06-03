package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.cache.HyenaCacheFactory;
import io.github.alphajiang.hyena.biz.calculator.CostCalculator;
import io.github.alphajiang.hyena.biz.calculator.PointRecCalculator;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.*;
import io.github.alphajiang.hyena.ds.service.FreezeOrderRecDs;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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

    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Override
    public CalcType getType() {
        return CalcType.REFUND;
    }


    @Override
    @Transactional
    public Mono<PSession> process(PSession session) {
        PointUsage usage = session.getUsage();
        log.info("refund. usage = {}", usage);

//        try (PointWrapper pw = ) {
            return preProcess(session, true, true)
                    .flatMap(sess -> {
                        PointCache p = sess.getPw().getPointCache();
                        if (usage.getUnfreezePoint() != null
                                && DecimalUtils.gt(usage.getUnfreezePoint(), DecimalUtils.ZERO)) {

                            PointUsage usage4Unfreeze = new PointUsage();
                            BeanUtils.copyProperties(usage, usage4Unfreeze);
                            usage4Unfreeze.setPoint(usage.getUnfreezePoint())
                                    .setDoUpdate(false);

                            return Mono.just(PSession.fromUsage(usage4Unfreeze))
                                    .doOnNext(sess4Unfreeze -> sess4Unfreeze.setPw(sess.getPw()))
                                    .flatMap(sess4Unfreeze -> this.pointUnfreezeStrategy.process(sess4Unfreeze))
                                    .flatMap(sess4Unfreeze -> {

                                                List<FreezeOrderRecPo> forList = sess4Unfreeze.getResult().getUpdateQ().getFoList();
                                        PointOpResult result =   this.processPoint(usage, p, forList, sess4Unfreeze.getResult());
                                        sess.setResult(result);
                                        return
                                                hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                                                        usage.getUid(), usage.getSubUid(), p.getPoint())
                                                        .map(x -> sess);
                                            });


                        } else {
                            PointOpResult result = this.processPoint(usage, p);
                            sess.setResult(result);
                            return Mono.just(sess);
                        }
                    })
                    .doFinally(x -> {
                        if(session.getPw() != null) {
                            session.getPw().close();
                        }
                    });

    }


    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        log.info(">> pointCache = {}", pointCache);
        //PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), true);
        HyenaAssert.notNull(usage.getCost(), "invalid parameter: cost");
        PointPo curPoint = pointCache.getPoint();
        BigDecimal availableCost = curPoint.getCost().subtract(curPoint.getFrozenCost());
        HyenaAssert.isTrue(DecimalUtils.gte(availableCost, usage.getCost()),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");


        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = new PointPo();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);
        var recLogsRet = this.loop(usage.getType(), pointCache, pointLog, usage.getCost());

        curPoint.setPoint(curPoint.getPoint().subtract(recLogsRet.getDelta()))
                .setAvailable(curPoint.getAvailable().subtract(recLogsRet.getDelta()))
                .setRefund(curPoint.getRefund().add(recLogsRet.getDelta()))
                .setCost(curPoint.getCost().subtract(recLogsRet.getDeltaCost()));
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
                .setOpCost(recLogsRet.getDeltaCost())
                .setLogs(List.of(PointLogDto.build(pointLog)));

        hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                        usage.getUid(), usage.getSubUid(), pointCache.getPoint())
                .subscribe();

        log.info("<< pointCache = {}", pointCache);
        return ret;


    }

    public PointOpResult processPoint(PointUsage usage, PointCache pointCache,
                                      List<FreezeOrderRecPo> forList,
                                      PointOpResult unfreezeRet) {
        log.info(">> pointCache = {}", pointCache);

        PointPo curPoint = pointCache.getPoint();
        BigDecimal availableCost = curPoint.getCost().subtract(curPoint.getFrozenCost());
        HyenaAssert.isTrue(DecimalUtils.gte(availableCost, usage.getCost()),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available cost");

        BigDecimal availablePoint = curPoint.getPoint().subtract(curPoint.getFrozen());
        HyenaAssert.isTrue(DecimalUtils.gte(availablePoint, usage.getPoint()),
                HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point");

        curPoint.setSeqNum(curPoint.getSeqNum() + 1);
        var point2Update = unfreezeRet.getUpdateQ().getPoint();
        point2Update.setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);

        LoopResult recLogsRet = this.refundLoop(usage, pointCache, pointLog, forList);


        curPoint.setPoint(curPoint.getPoint().subtract(recLogsRet.getDelta()))
                .setAvailable(curPoint.getAvailable().subtract(recLogsRet.getDelta()))
                .setRefund(curPoint.getRefund().add(recLogsRet.getDelta()))
                .setCost(curPoint.getCost().subtract(recLogsRet.getDeltaCost()));
        point2Update.setPoint(curPoint.getPoint())
                .setAvailable(curPoint.getAvailable())
                .setRefund(curPoint.getRefund())
                .setCost(curPoint.getCost());

        pointLog = this.pointBuilder.buildPointLog(PointOpType.REFUND, usage, curPoint);
        pointLog.setDelta(recLogsRet.getDelta())
                .setDeltaCost(recLogsRet.getDeltaCost());

        pointFlowService.updatePoint(usage.getType(), point2Update);

        unfreezeRet.getUpdateQ().getRecList().addAll(recLogsRet.getRecList4Update());
        pointFlowService.updatePointRec(usage.getType(), unfreezeRet.getUpdateQ().getRecList());

        if (CollectionUtils.isNotEmpty(unfreezeRet.getUpdateQ().getFoList())) {
            pointFlowService.closeFreezeOrderRec(usage.getType(), unfreezeRet.getUpdateQ().getFoList());
        }

        unfreezeRet.getUpdateQ().getLogs().add(pointLog);
        unfreezeRet.getUpdateQ().getRecLogs().addAll(recLogsRet.getRecLogs());
        pointFlowService.addFlow(usage, unfreezeRet.getUpdateQ().getLogs(),
                unfreezeRet.getUpdateQ().getRecLogs());
        //return pointCache.getPoint();

        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost())
                .setLogs(List.of(PointLogDto.build(pointLog)));
        log.info("<< pointCache = {}", pointCache);
        return ret;
    }


    private LoopResult loop(String type, PointCache pointCache,
                            PointLogPo pointLog, BigDecimal expected) {
        log.info("refund cost. type = {}, uid = {}, expected = {}", type, pointCache.getPoint().getUid(), expected);

        LoopResult result = new LoopResult();
        BigDecimal sum = DecimalUtils.ZERO;
        BigDecimal sumPoint = DecimalUtils.ZERO;
        BigDecimal cost = DecimalUtils.ZERO;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogDto> recLogs = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            BigDecimal gap = expected.subtract(sum);
            BigDecimal availableCost = this.costCalculator.getAvailableCost(rec);
            if (DecimalUtils.lte(gap, DecimalUtils.ZERO)) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (DecimalUtils.lte(availableCost, DecimalUtils.ZERO)) {
                // do nothing
            } else if (DecimalUtils.lt(availableCost, gap)) {
                sum = sum.add(availableCost);
                BigDecimal deltaCost = availableCost;
                BigDecimal delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint = sumPoint.add(delta);
                cost = cost.add(deltaCost);
                var rec4Update = this.pointRecCalculator.refundPoint(rec, delta, deltaCost);
                recList4Update.add(rec4Update);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, deltaCost);
                recLogs.add(recLog);
            } else {
                sum = sum.add(gap);
                BigDecimal deltaCost = gap;
                BigDecimal delta = this.costCalculator.accountPoint(rec, deltaCost);
                sumPoint = sumPoint.add(delta);
                cost = cost.add(deltaCost);
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
                l.setFrozen(l.getFrozen().add(fo.getFrozen()))
                        .setFrozenCost(l.getFrozenCost().add(fo.getFrozenCost()));
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
        BigDecimal delta = DecimalUtils.ZERO;
        BigDecimal deltaCost = DecimalUtils.ZERO;
        for (var fr : forList) {
            delta = delta.add(fr.getFrozen());
            deltaCost = deltaCost.add(fr.getFrozenCost());
        }
        //        long delta = forList.stream().mapToLong(FreezeOrderRecPo::getFrozen).sum();
//        long deltaCost = forList.stream().mapToLong(FreezeOrderRecPo::getFrozenCost).sum();
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
