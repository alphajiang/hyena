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
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
        try (PointWrapper pw = preProcess(usage, true, true)) {
            PointCache p = pw.getPointCache();
            return this.processPoint(usage, p);
        } catch (Exception e) {
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
            if (usage.getCost() < 1L) {
                throw new HyenaParameterException("invalid parameter cost");
            }
        } else {
            HyenaAssert.isTrue(usage.getPoint() > 0L, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                    "invalid parameter, 'point' must great than 0");
        }
        this.cusPointTableDs.getOrCreateTable(usage.getType());
        //logger.debug("tableName = {}", tableName);

        if (fetchPoint) {
            PointWrapper pw = this.pointMemCacheService.getPoint(usage.getType(), usage.getUid(), true);
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
        private long delta;
        private long deltaCost;
        private List<PointRecPo> recList4Update;
        private List<PointRecLogPo> recLogs;
        private List<FreezeOrderRecPo> forList;
    }

}
