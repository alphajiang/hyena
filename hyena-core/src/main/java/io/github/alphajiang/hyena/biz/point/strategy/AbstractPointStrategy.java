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
import io.github.alphajiang.hyena.biz.cache.HyenaCacheFactory;
import io.github.alphajiang.hyena.biz.point.PSession;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointVo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.HyenaLockService;
import lombok.Data;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
abstract class AbstractPointStrategy implements PointStrategy {

    @Autowired
    private PointTableDs cusPointTableDs;

    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private HyenaLockService hyenaLockService;

    @PostConstruct
    public void init() {
        PointStrategyFactory.addStrategy(this);
    }

    abstract PointOpResult processPoint(PointUsage usage, PointCache p);

    @Override
    public Mono<PSession> process(PSession session) {
        log.info("usage = {}", session.getUsage());
        PointUsage usage = session.getUsage();
//        if (session.getPw() == null) {
//            boolean localLockRet = hyenaLockService.lock(usage.getUid(), usage.getSubUid());
//            if (!localLockRet) {
//                log.error("get lock timeout!!! usage = {}", usage);
//                throw new HyenaServiceException("get lock timeout, retry later");
//            }
//        }

        return preProcess(session, session.getPw() == null, true)
                .flatMap(pw -> {

                    PointCache p = session.getPw().getPointCache();
//                    if (usage.getPw() != null) {
//                        p = usage.getPw().getPointCache();
//                    } else {
//                        p = pw.getPointCache();
//                    }

                    session.setOriginPoint(new PointVo());
                    BeanUtils.copyProperties(p.getPoint(), session.getOriginPoint());
                    PointOpResult result = this.processPoint(usage, p);
                    session.setResult(result);
                    if (usage.isDoUpdate()) {
                        return hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                                        usage.getUid(), usage.getSubUid(), p.getPoint())
                                .map(x -> session);
                    } else {
                        return Mono.just(session);
                    }
                })
                .doOnError(err -> {
                    log.error("error = {}", err.getMessage(), err);
                    log.error("session data = {}", session);
                    if (session.getOriginPoint() != null
                            && session.getPw() != null
                            && session.getPw().getPointCache() != null
                            && session.getPw().getPointCache().getPoint() != null) {
                        // 回滚缓存的数据
                        BeanUtils.copyProperties(session.getOriginPoint(), session.getPw().getPointCache().getPoint());
                    }
                    hyenaCacheFactory.getPointCacheService().unlock(usage.getType(), usage.getUid(), usage.getSubUid())
                            .subscribe();
                })
                .doFinally(pw -> {
                    if(session.getPw() != null) {
                        session.getPw().close();
                    }
//                    hyenaLockService.unlock(usage.getUid(), usage.getSubUid());
                });


    }


    Mono<PSession> preProcess(PSession session) {
        return this.preProcess(session, false);
    }

    Mono<PSession> preProcess(PSession session, boolean fetchPoint) {
        return this.preProcess(session, fetchPoint, false);
    }

    Mono<PSession> preProcess(PSession session, boolean fetchPoint, boolean mustExist) {
        //String tableName =
        PointUsage usage = session.getUsage();
        HyenaAssert.notBlank(usage.getType(), "invalid parameter, 'type' can't blank");
        HyenaAssert.notBlank(usage.getUid(), "invalid parameter, 'uid' can't blank");
//        if (usage.getPw() != null) {
//            return usage.getPw();
//        }
        if (getType() == CalcType.INCREASE || getType() == CalcType.EXPIRE) {

        } else if ((getType() == CalcType.FREEZE_COST || getType() == CalcType.REFUND)
                && usage.getCost() != null) {
            if (DecimalUtils.lt(usage.getCost(), BigDecimal.ZERO)) {
                throw new HyenaParameterException("invalid parameter cost");
            }
        } else {
            HyenaAssert.isTrue(usage.getPoint() != null && usage.getPoint().compareTo(DecimalUtils.ZERO) > 0,
                    HyenaConstants.RES_CODE_PARAMETER_ERROR,
                    "invalid parameter, 'point' must great than 0");
        }
        this.cusPointTableDs.getOrCreateTable(usage.getType());
        //logger.debug("tableName = {}", tableName);

        if (fetchPoint) {
            return this.hyenaCacheFactory.getPointCacheService()
                    .getPoint(usage.getType(), usage.getUid(), usage.getSubUid(), true)
                    .doOnNext(pw -> {
                        if (mustExist && pw.getPointCache().getPoint() == null) {
                            pw.close();
                            throw new HyenaParameterException("account not exist");
                        }
                    })
                    .map(pw -> {
                        session.setPw(pw);
                        return session;
                    });
        } else {
            return Mono.just(session);
        }
    }


    @Data
    @Accessors(chain = true)
    @ToString
    public static class LoopResult {
        private BigDecimal delta;
        private BigDecimal deltaCost;
        @Exclude
        private List<PointRecPo> recList4Update;
        @Exclude
        private List<PointRecLogDto> recLogs;
        @Exclude
        private List<FreezeOrderRecPo> forList;
    }

}
