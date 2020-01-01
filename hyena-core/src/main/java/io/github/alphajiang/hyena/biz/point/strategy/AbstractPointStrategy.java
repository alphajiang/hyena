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
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointVo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
abstract class AbstractPointStrategy implements PointStrategy {

    @Autowired
    private PointTableDs cusPointTableDs;

    @Autowired
    private PointMemCacheService pointMemCacheService;

    @PostConstruct
    public void init() {
        PointStrategyFactory.addStrategy(this);
    }

    abstract PointOpResult processPoint(PointUsage usage, PointCache p);

    @Override
    public PointOpResult process(PointUsage usage) {
        log.info("usage = {}", usage);
        PointVo backup = null;
        PointVo point = null;
        try (PointWrapper pw = preProcess(usage, true, true)) {
            PointCache p = pw.getPointCache();
            point = p.getPoint();
            backup = new PointVo();
            BeanUtils.copyProperties(point, backup);
            return this.processPoint(usage, p);
        } catch (Exception e) {
            if (point != null && backup != null) {
                // 回滚缓存的数据
                BeanUtils.copyProperties(backup, point);
            }
            throw e;
        }
    }


    PointWrapper preProcess(PointUsage usage) {
        return this.preProcess(usage, false);
    }

    PointWrapper preProcess(PointUsage usage, boolean fetchPoint) {
        return this.preProcess(usage, fetchPoint, false);
    }

    PointWrapper preProcess(PointUsage usage, boolean fetchPoint, boolean mustExist) {
        //String tableName =
        HyenaAssert.notBlank(usage.getType(), "invalid parameter, 'type' can't blank");
        HyenaAssert.notBlank(usage.getUid(), "invalid parameter, 'uid' can't blank");
        if (getType() == CalcType.INCREASE) {

        } else if ((getType() == CalcType.FREEZE_COST || getType() == CalcType.REFUND)
                && usage.getCost() != null) {
            if (usage.getCost().compareTo(DecimalUtils.ZERO) <= 0) {
                throw new HyenaParameterException("invalid parameter cost");
            }
        } else {
            HyenaAssert.isTrue(usage.getPoint().compareTo(DecimalUtils.ZERO) > 0,
                    HyenaConstants.RES_CODE_PARAMETER_ERROR,
                    "invalid parameter, 'point' must great than 0");
        }
        this.cusPointTableDs.getOrCreateTable(usage.getType());
        //logger.debug("tableName = {}", tableName);

        if (fetchPoint) {
            PointWrapper pw = this.pointMemCacheService.getPoint(usage.getType(), usage.getUid(),  usage.getSubUid(),true);
            if (mustExist && pw.getPointCache().getPoint() == null) {
                pw.close();
                throw new HyenaParameterException("account not exist");
            }
            return pw;
        } else {
            return null;
        }
    }


    @Data
    @Accessors(chain = true)
    public static class LoopResult {
        private BigDecimal delta;
        private BigDecimal deltaCost;
        private List<PointRecPo> recList4Update;
        private List<PointRecLogDto> recLogs;
        private List<FreezeOrderRecPo> forList;
    }

}
