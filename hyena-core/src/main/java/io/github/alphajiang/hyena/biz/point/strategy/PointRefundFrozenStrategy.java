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

//import io.github.alphajiang.hyena.biz.flow.PointFlowService;
//import io.github.alphajiang.hyena.biz.point.PointCache;
//import io.github.alphajiang.hyena.biz.point.PointUsage;
//import io.github.alphajiang.hyena.biz.point.PointWrapper;
//import io.github.alphajiang.hyena.ds.service.*;
//import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
//import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
//import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
//import io.github.alphajiang.hyena.model.po.PointPo;
//import io.github.alphajiang.hyena.model.type.CalcType;
//import io.github.alphajiang.hyena.model.vo.PointOpResult;
//import io.github.alphajiang.hyena.utils.CollectionUtils;
//import io.github.alphajiang.hyena.utils.DecimalUtils;
//import io.github.alphajiang.hyena.utils.StringUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Slf4j
//@Component
//public class PointRefundFrozenStrategy extends AbstractPointStrategy {
//
//    @Autowired
//    private PointDs pointDs;
//
//    @Autowired
//    private PointLogDs pointLogDs;
//
//    @Autowired
//    private PointRecDs pointRecDs;
//
//    @Autowired
//    private PointRecLogDs pointRecLogDs;
//
//    @Autowired
//    private PointFlowService pointFlowService;
//
//    @Autowired
//    private PointUnfreezeStrategy pointUnfreezeStrategy;
//
//    @Autowired
//    private PointRefundStrategy pointRefundStrategy;
//
//    private FreezeOrderRecDs freezeOrderRecDs;
//
//    @Override
//    public CalcType getType() {
//        return CalcType.REFUND_FROZEN;
//    }
//
//    @Override
//    @Transactional
//    public PointOpResult process(PointUsage usage) {
//        log.info("refund frozen. usage = {}", usage);
//        PointPo ret = null;
//
//        if (StringUtils.isBlank(usage.getOrderNo())) {
//            log.warn("订单号不能为空. usage = {}", usage);
//            throw new HyenaParameterException("订单号不能为空");
//        }
//
//        try (PointWrapper pw = preProcess(usage, true, true)) {
//            PointCache p = pw.getPointCache();
//            List<FreezeOrderRecPo> forList = this.freezeOrderRecDs.getFreezeOrderRecList(usage.getType(),
//                    p.getPoint().getId(),
//                    usage.getOrderType(), usage.getOrderNo());
//            if(CollectionUtils.isEmpty(forList)) {
//                PointOpResult retx = new PointOpResult();
//                BeanUtils.copyProperties(p, retx);
//                retx.setOpPoint(BigDecimal.ZERO)
//                        .setOpCost(BigDecimal.ZERO);
//                log.warn("<< 未找到与订单号对应的冻结积分. usage = {}", usage);
//                return retx;
//            }
//            if (usage.getUnfreezePoint() != null
//                    && usage.getUnfreezePoint().compareTo(DecimalUtils.ZERO) > 0) {
//                PointUsage usage4Unfreeze = new PointUsage();
//                BeanUtils.copyProperties(usage, usage4Unfreeze);
//                usage4Unfreeze.setPoint(usage.getUnfreezePoint());
//
//                this.pointUnfreezeStrategy.process(usage4Unfreeze);
//            }
//
//            return this.pointRefundStrategy.process(usage);
//        } catch (Exception e) {
//            throw e;
//        }
//
//    }
//
//    @Override
//    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
//        throw new HyenaServiceException("invalid logic");
//    }
//
//}
