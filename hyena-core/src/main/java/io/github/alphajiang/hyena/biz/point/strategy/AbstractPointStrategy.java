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
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
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
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private HyenaLockService hyenaLockService;

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
        if (usage.getPw() == null) {
            boolean localLockRet = hyenaLockService.lock(usage.getUid(), usage.getSubUid());
            if (!localLockRet) {
                log.error("get lock timeout!!! usage = {}", usage);
                throw new HyenaServiceException("get lock timeout, retry later");
            }
        }
        try {
            try (PointWrapper pw = preProcess(usage, usage.getPw() == null, true)) {
                PointCache p;
                if (usage.getPw() != null) {
                    p = usage.getPw().getPointCache();
                } else {
                    p = pw.getPointCache();
                }
                point = p.getPoint();
                backup = new PointVo();
                BeanUtils.copyProperties(point, backup);
                PointOpResult result = this.processPoint(usage, p);
                if (usage.isDoUpdate()) {
                    hyenaCacheFactory.getPointCacheService().updatePoint(usage.getType(),
                            usage.getUid(), usage.getSubUid(), p.getPoint());
                }
                //hyenaCacheFactory.getPointCacheService().un
                return result;
            } catch (Exception e) {
                log.warn("exception: {}", e.getMessage(), e);
                if (point != null && backup != null) {
                    // 回滚缓存的数据
                    BeanUtils.copyProperties(backup, point);
                }
                hyenaCacheFactory.getPointCacheService().unlock(usage.getType(), usage.getUid(), usage.getSubUid());

                throw e;
            }
        } catch (Exception e3) {
            throw e3;
        } finally {
            if (usage.getPw() == null) {
                hyenaLockService.unlock(usage.getUid(), usage.getSubUid());
            }
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
//        if (usage.getPw() != null) {
//            return usage.getPw();
//        }
        if (getType() == CalcType.INCREASE || getType() == CalcType.EXPIRE) {

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
            PointWrapper pw = this.hyenaCacheFactory.getPointCacheService()
                    .getPoint(usage.getType(), usage.getUid(), usage.getSubUid(), true);
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
