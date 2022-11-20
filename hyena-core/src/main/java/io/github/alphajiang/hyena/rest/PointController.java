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

package io.github.alphajiang.hyena.rest;

import io.github.alphajiang.hyena.aop.Idempotent;
import io.github.alphajiang.hyena.biz.cache.HyenaCacheFactory;
import io.github.alphajiang.hyena.biz.point.PSession;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.param.ListPointRecLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.PointCancelParam;
import io.github.alphajiang.hyena.model.param.PointDecreaseFrozenParam;
import io.github.alphajiang.hyena.model.param.PointDecreaseParam;
import io.github.alphajiang.hyena.model.param.PointFreezeByRecIdParam;
import io.github.alphajiang.hyena.model.param.PointFreezeParam;
import io.github.alphajiang.hyena.model.param.PointIncreaseParam;
import io.github.alphajiang.hyena.model.param.PointOpParam;
import io.github.alphajiang.hyena.model.param.PointRefundParam;
import io.github.alphajiang.hyena.model.param.PointUnfreezeParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.model.vo.PointLogBi;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DateUtils;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaLockService;
import io.github.alphajiang.hyena.utils.LoggerHelper;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "积分相关的接口", description = "积分")
@RequestMapping(value = "/hyena/point", produces = MediaType.APPLICATION_JSON_VALUE)
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private HyenaLockService lockService;

    @Operation(summary = "获取积分信息")
    @GetMapping(value = "/getPoint")
    public Mono<ObjectResponse<PointPo>> getPoint(ServerWebExchange exh,
        @Parameter(description = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
        @Parameter(description = "用户ID") @RequestParam String uid,
        @Parameter(description = "用户二级ID") @RequestParam(required = false) String subUid,
        @Parameter(description = "是否精准的") @RequestParam(required = false) Boolean exact) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        var ret =
            this.hyenaCacheFactory.getPointCacheService().getPoint(type, uid, subUid, false);
        return ret.map(o -> new ObjectResponse<>(PointPo.copy(o.getPointCache().getPoint())))
            .doOnNext(o -> logger.info(LoggerHelper.formatLeaveLog(exh)));
    }

    @Operation(summary = "获取积分列表")
    @PostMapping(value = "/listPoint")
    public ListResponse<PointPo> listPoint(ServerWebExchange exh,
        @RequestBody ListPointParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);

        param.setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)));
        var res = this.pointDs.listPoint4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @Operation(summary = "获取变更明细列表")
    @PostMapping(value = "/listPointLog")
    public ListResponse<PointLogDto> listPointLog(ServerWebExchange exh,
        @RequestBody ListPointLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        if (CollectionUtils.isEmpty(param.getSorts())) {
            param.setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        }
        var res = this.pointLogDs.listPointLog4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @Operation(summary = "获取变更明细统计")
    @PostMapping(value = "/listPointLogBi")
    public ListResponse<PointLogBi> listPointLogBi(ServerWebExchange exh,
        @RequestBody ListPointLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        var list = this.pointLogDs.listPointLogBi(param);
        var res = new ListResponse<>(list, list.size());
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @Operation(summary = "获取记录列表")
    @PostMapping(value = "/listPointRecord")
    public ListResponse<PointRecDto> listPointRecord(ServerWebExchange exh,
        @RequestBody ListPointRecParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + "param = {}", param);
        if (CollectionUtils.isEmpty(param.getSorts())) {
            param.setSorts(List.of(SortParam.as("rec.id", SortOrder.desc)));
        }
        var res = this.pointRecDs.listPointRec4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @Operation(summary = "获取记录历史明细列表")
    @PostMapping(value = "/listPointRecordLog")
    public ListResponse<PointRecLogDto> listPointRecordLog(ServerWebExchange exh,
        @RequestBody ListPointRecLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + "param = {}", param);
        if (param.getSorts() == null) {
            param.setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        }
        var res = this.pointRecLogDs.listPointRecLog4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @Idempotent(name = "increase-point", resClass = ObjectResponse.class)
    @Operation(summary = "增加用户积分")
    @PostMapping(value = "/increase")
    public Mono<ObjectResponse<PointPo>> increasePoint(ServerWebExchange exh,
        @RequestBody @NotNull PointIncreaseParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        if (usage.getPoint() == null || DecimalUtils.ltZero(usage.getPoint())) {
            throw new HyenaParameterException("invalid parameter point");
        }

        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.increase(PSession.fromUsage(usage)))
            .map(sess -> new ObjectResponse<>((PointPo) sess.getResult()))
            .doOnNext(rt -> {
                logger.info(LoggerHelper.formatLeaveLog(exh));
                debugPerformance(exh, startTime);
            })
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "decrease-point", resClass = ObjectResponse.class)
    @Operation(summary = "消费用户积分")
    @PostMapping(value = "/decrease")
    public Mono<ObjectResponse<PointOpResult>> decreasePoint(ServerWebExchange exh,
        @RequestBody PointDecreaseParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setRecId(param.getRecId());
        if (usage.getPoint() == null || DecimalUtils.ltZero(usage.getPoint())) {
            throw new HyenaParameterException("invalid parameter point");
        }
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.decrease(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "decreaseFrozen-point", resClass = ObjectResponse.class)
    @Operation(summary = "消费已冻结的用户积分")
    @PostMapping(value = "/decreaseFrozen")
    public Mono<ObjectResponse<PointOpResult>> decreaseFrozenPoint(ServerWebExchange exh,
        @RequestBody PointDecreaseFrozenParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointDecreaseParam(param);
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn ->
                this.pointUsageFacade.decreaseFrozen(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "freeze-point", resClass = ObjectResponse.class)
    @Operation(summary = "冻结用户积分")
    @PostMapping(value = "/freeze")
    public Mono<ObjectResponse<PointOpResult>> freezePoint(ServerWebExchange exh,
        @RequestBody PointOpParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn ->
                this.pointUsageFacade.freeze(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "unfreeze-point", resClass = ObjectResponse.class)
    @Operation(summary = "解冻用户积分")
    @PostMapping(value = "/unfreeze")
    public Mono<ObjectResponse<PointOpResult>> unfreezePoint(ServerWebExchange exh,
        @RequestBody PointUnfreezeParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.unfreeze(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "cancel-point", resClass = ObjectResponse.class)
    @Operation(summary = "撤销用户积分")
    @PostMapping(value = "/cancel")
    public Mono<ObjectResponse<PointOpResult>> cancelPoint(ServerWebExchange exh,
        @RequestBody PointCancelParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);

        long startTime = System.nanoTime();
        PointUsage usage = PointUsageBuilder.fromPointCancelParam(param);
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.cancel(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "freeze-by-rec-id", resClass = ObjectResponse.class)
    @Operation(summary = "按积分块冻结")
    @PostMapping(value = "/freezeByRecId")
    public Mono<ObjectResponse<PointOpResult>> freezeByRecId(ServerWebExchange exh,
        @RequestBody PointFreezeByRecIdParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        //        if (param.getUnfreezePoint() != null && param.getUnfreezePoint() < 0L) {
        //            throw new HyenaParameterException("invalid parameter: unfreezePoint");
        //        }
        long startTime = System.nanoTime();
        PointUsage usage = PointUsageBuilder.fromPointFreezeByRecIdParam(param);
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.freezeByRecId(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Idempotent(name = "freeze-cost", resClass = ObjectResponse.class)
    @Operation(summary = "按成本冻结")
    @PostMapping(value = "/freezeCost")
    public Mono<ObjectResponse<PointOpResult>> freezeCost(ServerWebExchange exh,
        @RequestBody PointFreezeParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        long startTime = System.nanoTime();
        PointUsage usage = PointUsageBuilder.fromPointFreezeParam(param);
        if (usage.getCost() == null || DecimalUtils.ltZero(usage.getCost())) {
            throw new HyenaParameterException("invalid parameter cost");
        } else if (StringUtils.isBlank(usage.getOrderNo())) {
            throw new HyenaParameterException("invalid parameter 'orderNo'");
        }
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.freezeCost(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    //    @Idempotent(name = "unfreeze-cost")
    //    @Operation(summary = "按成本解冻")
    //    @PostMapping(value = "/unfreezeCost")
    //    public Mono<ObjectResponse<PointOpResult>> unfreezeCost(ServerWebExchange exh, @RequestBody PointUnfreezeParam param) {
    //        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
    //        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
    //        if (StringUtils.isBlank(usage.getOrderNo())) {
    //            throw new HyenaParameterException("invalid parameter 'orderNo'");
    //        }
    //        return this.pointUsageFacade.unfreezeCost(PSession.fromUsage(usage)).map(sess -> mapObjResult(exh, sess, null));
    //    }

    @Idempotent(name = "refund", resClass = ObjectResponse.class)
    @Operation(summary = "退款")
    @PostMapping(value = "/refund")
    public Mono<ObjectResponse<PointOpResult>> refund(ServerWebExchange exh,
        @RequestBody PointRefundParam param) {
        logger.info(LoggerHelper.formatEnterLog(exh, false) + " param = {}", param);
        long startTime = System.nanoTime();
        PointUsage usage = PointUsageBuilder.fromPointRefundParam(param);
        if (usage.getCost() == null || DecimalUtils.ltZero(usage.getCost())) {
            throw new HyenaParameterException("invalid parameter cost");
        } else if (usage.getPoint() == null || DecimalUtils.ltZero(usage.getPoint())) {
            throw new HyenaParameterException("invalid parameter point");
        }
        usage.setUnfreezePoint(param.getUnfreezePoint());
        return Mono.just(usage)
            .doOnNext(
                usageIn -> lockService.lock(param.getType(), param.getUid(), param.getSubUid()))
            .flatMap(usageIn -> this.pointUsageFacade.refund(PSession.fromUsage(usage)))
            .map(sess -> mapObjResult(exh, sess, startTime))
            .doFinally(x -> lockService.unlock(param.getType(), param.getUid(), param.getSubUid()));
    }

    @Operation(summary = "获取时间段内总共增加的积分数量")
    @GetMapping(value = "/getIncreasedPoint")
    public ObjectResponse<BigDecimal> getIncreasedPoint(ServerWebExchange exh,
        @Parameter(description = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
        @Parameter(description = "用户ID") @RequestParam(required = false) String uid,
        @Parameter(description = "开始时间", example = "2019-03-25 18:35:21") @RequestParam(required = false, value = "start") String strStart,
        @Parameter(description = "结束时间", example = "2019-04-26 20:15:31") @RequestParam(required = false, value = "end") String strEnd) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        try {
            Calendar calStart = DateUtils.fromYyyyMmDdHhMmSs(strStart);
            Calendar calEnd = DateUtils.fromYyyyMmDdHhMmSs(strEnd);
            var ret =
                this.pointRecDs.getIncreasedPoint(type, uid, calStart.getTime(), calEnd.getTime());

            ObjectResponse<BigDecimal> res = new ObjectResponse<>(ret);
            logger.info(LoggerHelper.formatLeaveLog(exh) + " ret = {}", ret);
            return res;
        } catch (ParseException e) {
            logger.warn(e.getMessage(), e);
            throw new HyenaParameterException("参数错误, 时间格式无法解析");
        }
    }

    @Operation(summary = "禁用帐号")
    @PostMapping(value = "/disableAccount")
    public BaseResponse disableAccount(ServerWebExchange exh,
        @Parameter(description = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
        @Parameter(description = "用户ID") @RequestParam String uid,
        @Parameter(description = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        this.pointDs.disableAccount(type, uid, subUid);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return BaseResponse.success();
    }

    private ObjectResponse<PointOpResult> mapObjResult(ServerWebExchange exh, PSession sess,
        Long startTime) {
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(sess.getResult());
        logger.info(LoggerHelper.formatLeaveLog(exh));
        if (startTime != null) {
            debugPerformance(exh, startTime);
        }
        return res;
    }

    private void debugPerformance(ServerWebExchange exh, long startTime) {
        long curTime = System.nanoTime();
        if (curTime - startTime > 2000L * 1000000) {
            logger.warn("延迟过大...{}. url = {}", (curTime - startTime) / 1000000,
                exh.getRequest().getPath().toString());
        }
    }
}
