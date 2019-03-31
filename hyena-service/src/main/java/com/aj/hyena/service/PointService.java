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

package com.aj.hyena.service;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.mapper.PointMapper;
import com.aj.hyena.model.exception.HyenaNoPointException;
import com.aj.hyena.model.param.ListPointRecParam;
import com.aj.hyena.model.param.SortParam;
import com.aj.hyena.model.po.PointPo;
import com.aj.hyena.model.po.PointRecPo;
import com.aj.hyena.model.type.SortOrder;
import com.aj.hyena.utils.HyenaAssert;
import com.aj.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointService {
    private static final Logger logger = LoggerFactory.getLogger(PointService.class);

    @Autowired
    private PointMapper cusPointMapper;


    @Autowired
    private PointTableService cusPointTableService;

    @Autowired
    private PointRecService pointRecService;

    public PointPo getCusPoint(String type, String cusId, boolean lock) {
        String tableName = TableNameHelper.getPointTableName(type);
        var ret = this.cusPointMapper.getCusPoint(tableName, cusId, lock);
        return ret;
    }

    /**
     * 增加积分
     *
     * @param type
     * @param cusId
     * @param point
     * @return
     */
    @Transactional
    public PointPo increasePoint(String type, String cusId, Long point) {
        logger.info("increase. type = {}, cusId = {}, point = {}", type, cusId, point);
        String tableName = this.cusPointTableService.getOrCreateTable(type);
        logger.debug("tableName = {}", tableName);
        var cusPoint = this.cusPointMapper.getCusPoint(tableName, cusId, true);
        if (cusPoint == null) {
            this.cusPointMapper.addPoint(tableName, cusId, point);
            cusPoint = this.cusPointMapper.getCusPoint(tableName, cusId, false);
        } else {
            cusPoint.setPoint(cusPoint.getPoint() + point)
                    .setAvailable(cusPoint.getAvailable() + point);
            this.cusPointMapper.updateCusPoint(tableName, cusPoint);
        }
        this.pointRecService.addPointRec(type, cusPoint.getId(), point, "", null, null);
        return cusPoint;
    }

    @Transactional
    public long decreasePoint(String type, String cusId, long point, String note) {
        logger.info("decrease. type = {}, cusId = {}, point = {}", type, cusId, point);
        PointPo curPoint = this.getCusPoint(type, cusId, true);
        logger.debug("curPoint = {}", curPoint);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the cusId: " + cusId, Level.WARN);
        HyenaAssert.notNull(curPoint.getAvailable(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the cusId: " + cusId, Level.WARN);
        HyenaAssert.isTrue(curPoint.getAvailable().longValue() >= point, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point. current available point is " + curPoint.getAvailable());


        long gap = point;

        try {
            do {
                gap = this.decreasePointLoop(type, cusId, gap, note);
                logger.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        HyenaAssert.isTrue(gap == 0L, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough available point!");
        curPoint.setPoint(curPoint.getPoint() - point).setAvailable(curPoint.getAvailable() - point)
                .setUsed(curPoint.getUsed() + point);
        this.update(type, curPoint);
        return gap;
    }

    private long decreasePointLoop(String type, String cusId, long point, String note) {
        logger.info("decrease. type = {}, cusId = {}, point = {}", type, cusId, point);
        ListPointRecParam param = new ListPointRecParam();
        param.setCusId(cusId).setAvailable(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecService.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        long sum = 0L;
        for (PointRecPo rec : recList) {
            long gap = point - sum;
            if (gap < 1L) {
                logger.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                this.pointRecService.decreasePoint(type, rec, gap, note);
            } else {
                sum += gap;
                this.pointRecService.decreasePoint(type, rec, gap, note);
                break;
            }
        }
        var ret = point - sum;
        logger.debug("ret = {}", ret);
        return ret;
    }

    @Transactional
    public long decreasePointUnfreeze(String type, String cusId, long point, String note) {
        logger.info("decrease unfreeze. type = {}, cusId = {}, point = {}", type, cusId, point);
        PointPo curPoint = this.getCusPoint(type, cusId, true);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the cusId: " + cusId, Level.WARN);
        HyenaAssert.notNull(curPoint.getFrozen(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the cusId: " + cusId, Level.WARN);
        HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= point, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point");

        long gap = point;
        try {
            do {
                gap = this.decreasePointUnfreezeLoop(type, cusId, gap, note);
                logger.debug("gap = {}", gap);
            } while (gap > 0L);
        } catch (HyenaNoPointException e) {

        }
        HyenaAssert.isTrue(gap == 0L, HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                "no enough frozen point!");
        curPoint.setPoint(curPoint.getPoint() - point).setFrozen(curPoint.getFrozen() - point)
                .setUsed(curPoint.getUsed() + point);
        this.update(type, curPoint);
        return gap;
    }

    private long decreasePointUnfreezeLoop(String type, String cusId, long point, String note) {
        logger.info("decrease unfreeze. type = {}, cusId = {}, point = {}", type, cusId, point);
        ListPointRecParam param = new ListPointRecParam();
        param.setCusId(cusId).setFrozen(true).setLock(true)
                .setSorts(List.of(SortParam.as("rec.id", SortOrder.asc)))
                .setSize(5);
        var recList = this.pointRecService.listPointRec(type, param);
        if (recList.isEmpty()) {
            throw new HyenaNoPointException("no enough point", Level.DEBUG);
        }
        long sum = 0L;
        for (PointRecPo rec : recList) {
            long gap = point - sum;
            if (gap < 1L) {
                logger.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getFrozen() < gap) {
                sum += rec.getFrozen();

                this.pointRecService.decreasePointUnfreeze(type, rec, gap, note);
            } else {
                sum += gap;
                this.pointRecService.decreasePointUnfreeze(type, rec, gap, note);
                break;
            }
        }
        var ret = point - sum;
        logger.debug("ret = {}", ret);
        return ret;
    }


    private void update(String type, PointPo point) {
        this.cusPointMapper.updateCusPoint(TableNameHelper.getPointTableName(type), point);
    }
}
