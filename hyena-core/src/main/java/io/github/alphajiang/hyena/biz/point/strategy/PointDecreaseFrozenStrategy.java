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

import io.github.alphajiang.hyena.biz.cache.HyenaCacheFactory;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PSession;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.*;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class PointDecreaseFrozenStrategy extends PointDecreaseStrategy {

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
    private FreezeOrderRecDs freezeOrderRecDs;

    @Autowired
    private PointUnfreezeStrategy pointUnfreezeStrategy;

    @Autowired
    private PointDecreaseStrategy pointDecreaseStrategy;

    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private HyenaLockService hyenaLockService;


    @Override
    public CalcType getType() {
        return CalcType.DECREASE_FROZEN;
    }

//    @Override
//    @Transactional
//    public PointOpResult process(PointUsage usage) {
//        log.info("decrease frozen. usage = {}", usage);
//        PointPo ret = null;
//
//        if (usage.getUnfreezePoint() != null
//                && usage.getUnfreezePoint().compareTo(DecimalUtils.ZERO) > 0) {
//            PointUsage usage4Unfreeze = new PointUsage();
//            BeanUtils.copyProperties(usage, usage4Unfreeze);
//            usage4Unfreeze.setPoint(usage.getUnfreezePoint());
//
//            this.pointUnfreezeStrategy.process(usage4Unfreeze);
//        }
//
//        return this.pointDecreaseStrategy.process(usage);
//
//
//    }


    @Override
    @Transactional
    public Mono<PSession> process(PSession session) {
        PointUsage usage = session.getUsage();
        log.info("decrease frozen. usage = {}", usage);
        if (usage.getUnfreezePoint() == null || DecimalUtils.lte(usage.getUnfreezePoint(), DecimalUtils.ZERO)) {
            // frozen number is zero, use decrease
            return this.pointDecreaseStrategy.process(session);
        }

//        boolean localLockRet = hyenaLockService.lock(usage.getUid(), usage.getSubUid());
//        if (!localLockRet) {
//            log.error("get lock timeout!!! usage = {}", usage);
//            throw new HyenaServiceException("get lock timeout, retry later");
//        }

        return preProcess(session, true, true)
                .flatMap(sess -> {
                    PointCache p = sess.getPw().getPointCache();

//            List<FreezeOrderRecPo> forList = this.freezeOrderRecDs.getFreezeOrderRecList(usage.getType(),
//                    p.getPoint().getId(),
//                    usage.getOrderType(), usage.getOrderNo());


                    PointUsage usage4Unfreeze = new PointUsage();
                    BeanUtils.copyProperties(usage, usage4Unfreeze);
                    usage4Unfreeze.setPoint(usage.getUnfreezePoint())
                            .setDoUpdate(false);

//                                .setPw(pw);
                    return Mono.just(PSession.fromUsage(usage4Unfreeze))
                            .doOnNext(sess4Unfreeze -> sess4Unfreeze.setPw(sess.getPw()))
                            .flatMap(sess4Unfreeze -> this.pointUnfreezeStrategy.process(sess4Unfreeze))
                            .flatMap(sess4Unfreeze -> {

                                List<FreezeOrderRecPo> forList = sess4Unfreeze.getResult().getUpdateQ().getFoList();
                                PointOpResult result = super.processPoint(usage, p, forList, sess4Unfreeze.getResult());
                                sess.setResult(result);
                                return hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                                                usage.getUid(), usage.getSubUid(), p.getPoint())
                                        .map(x -> sess);
                            });

                })
                .doFinally(x -> {
                    if (session.getPw() != null) {
                        session.getPw().close();
                    }
//                    hyenaLockService.unlock(usage.getUid(), usage.getSubUid());
                });


    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        throw new HyenaServiceException("invalid logic");
    }

}
