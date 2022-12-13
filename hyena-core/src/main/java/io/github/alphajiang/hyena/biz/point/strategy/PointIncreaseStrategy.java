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
import io.github.alphajiang.hyena.biz.point.PointBizService;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * 增加积分
 */
@Slf4j
@Component
public class PointIncreaseStrategy extends AbstractPointStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PointIncreaseStrategy.class);

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
    private HyenaCacheFactory hyenaCacheFactory;
//    @Autowired
//    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private PointBizService pointBizService;

    @Autowired
    private PointBuilder pointBuilder;

    @Override
    public CalcType getType() {
        return CalcType.INCREASE;
    }

    @Override
    @Transactional //(isolation = Isolation.READ_COMMITTED)
    public Mono<PSession> process(PSession session) {
        PointUsage usage = session.getUsage();
        logger.info("increase. usage = {}", usage);

        return super.preProcess(session, true, false)
            .flatMap(sess -> {
                PointWrapper pw = sess.getPw();
                if (pw.getPointCache().getPoint() == null) {
                    pointBizService.addPoint(usage, pw.getPointCache());
                } else if (DecimalUtils.gt(usage.getPoint(), DecimalUtils.ZERO)) {
                    this.updatePointCache(usage, pw.getPointCache());
                } else {
                    log.info("do nothing... usage = {}", usage);
                }
                PointOpResult ret = new PointOpResult();
                BeanUtils.copyProperties(pw.getPointCache().getPoint(), ret);
                ret.setOpPoint(usage.getPoint())
                    .setOpCost(usage.getCost());
                sess.setResult(ret);
                return hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                        usage.getUid(), usage.getSubUid(), pw.getPointCache().getPoint())
                    .map(x -> sess);
//                                .subscribe();
//                        return ret;
            })
            .doFinally(x -> {
                if (session.getPw() != null) {
                    session.getPw().close();
                }
//                        hyenaLockService.unlock(usage.getUid(), usage.getSubUid());
            });

    }

    // 创建新帐号



    private void updatePointCache(PointUsage usage, PointCache pc) {
        PointPo point = pc.getPoint();
        var point2Update = new PointPo();
        point.setSeqNum(point.getSeqNum() + 1L)
            .setPoint(point.getPoint().add(usage.getPoint())
                .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP))
            .setAvailable(point.getAvailable().add(usage.getPoint())
                .setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP));
        BigDecimal cost =
            usage.getCost() != null && DecimalUtils.gt(usage.getCost(), DecimalUtils.ZERO)
                ? usage.getCost() : DecimalUtils.ZERO;
        point.setCost(
            point.getCost().add(cost).setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP));
        if (StringUtils.isNotBlank(usage.getName())) {
            point.setName(usage.getName());
        }

        point2Update.setSeqNum(point.getSeqNum())
            .setPoint(point.getPoint())
            .setAvailable(point.getAvailable())
            .setCost(point.getCost())
            .setName(point.getName())
            .setId(point.getId());
        //point2Update.setCost(cost);

        this.pointFlowService.updatePoint(usage.getType(), point2Update);

        //this.pointDs.addPoint(usage.getType(), point2Update);

        //PointPo retPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);

        var pointRec = this.pointRecDs.addPointRec(usage, point.getId(), point.getSeqNum());
        //pointFlowService.insertPointRec(usage.getType(), pointRec);

        //pc.setPoint(this.pointDs.getPointVo(usage.getType(), point2Update.getId(), null));
        pc.getPoint().getRecList().add(pointRec);

        if (DecimalUtils.gt(usage.getPoint(), point.getPoint())) {
            // 之前有欠款 TODO: 待验证
            BigDecimal number = usage.getPoint().subtract(point.getPoint());
            PointRecPo rec4Update = new PointRecPo();
            rec4Update.setPid(pointRec.getPid())
                .setSeqNum(pointRec.getSeqNum())
                .setAvailable(pointRec.getAvailable().subtract(number))
                .setUsed(number);
            //this.pointRecDs.updatePointRec(usage.getType(), pointRec);
            this.pointFlowService.updatePointRec(usage.getType(), List.of(rec4Update));
        }

        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.INCREASE, usage, point);
        PointRecLogDto recLog = this.pointBuilder.buildRecLog(pointRec, pointLog, usage.getPoint(),
            cost);

        pointFlowService.addFlow(usage, pointLog, List.of(recLog));

    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        throw new HyenaServiceException("invalid logic");
    }
}
