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

package io.github.alphajiang.hyena.ds.service;

import io.github.alphajiang.hyena.ds.mapper.PointRecMapper;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointStatus;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PointRecService {
    private static final Logger logger = LoggerFactory.getLogger(PointRecService.class);


    @Autowired
    private PointRecMapper pointRecMapper;


    @Autowired
    private PointRecLogService pointRecLogService;

    public PointRecPo getById(String type, long id, boolean lock) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecMapper.getById(pointTableName, id, lock);
    }


    public List<PointRec> listPointRec(String type, ListPointRecParam param) {
        logger.debug("type = {}, param = {}", type, param);
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecMapper.listPointRec(pointTableName, param);
    }


    /**
     * 增加积分
     *
     * @param type       积分类型
     * @param pointId    积分ID
     * @param point      数量
     * @param tag        标签
     * @param expireTime 过期时间
     * @param note       备注
     */
    public void addPointRec(String type, long pointId, long point, String tag, Date expireTime, String note) {
        logger.info("type = {}, pointId = {}, point = {}, tag = {}, expireTime = {}, note = {}",
                type, pointId, point, tag, expireTime, note);
        PointRecPo rec = new PointRecPo();
        PointRecLogPo recLog = new PointRecLogPo();
        rec.setPid(pointId).setTotal(point).setAvailable(point);
        if (tag == null) {
            rec.setTag("");
            recLog.setTag("");
        } else {
            rec.setTag(tag);
            recLog.setTag(tag);
        }
        if (expireTime != null) {
            rec.setExpireTime(expireTime);
        }
//        if(!StringUtils.isEmpty(note)){
//            rec.setNote(note);
//        }
        String recTableName = TableNameHelper.getPointRecTableName(type);
        this.pointRecMapper.addPointRec(recTableName, rec);

        this.pointRecLogService.addLogByRec(type, PointStatus.INCREASE,
                rec, point, note);

    }

    public void decreasePoint(String type, PointRecPo rec, long point, String note) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            delta = rec.getAvailable();
            long used = rec.getUsed() + rec.getAvailable();
            rec.setAvailable(0L).setUsed(used);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long used = rec.getUsed() + point;
            rec.setAvailable(available).setUsed(used);
            this.updatePointRec(type, rec);

        }

        this.pointRecLogService.addLogByRec(type, PointStatus.DECREASE,
                rec, delta, note);
    }

    public void decreasePointUnfreeze(String type, PointRecPo rec, long point, String note) {

        long delta = point;
        if (rec.getFrozen() < delta) {
            delta = rec.getFrozen();
            long used = rec.getUsed() + rec.getFrozen();
            rec.setFrozen(0L).setUsed(used);
            this.updatePointRec(type, rec);
        } else {
            long frozen = rec.getFrozen() - point;
            long used = rec.getUsed() + point;
            rec.setFrozen(frozen).setUsed(used);
            this.updatePointRec(type, rec);

        }

        this.pointRecLogService.addLogByRec(type, PointStatus.DECREASE,
                rec, delta, note);
    }

    public void freezePoint(String type, PointRecPo rec, long point, String note) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            delta = rec.getAvailable();
            long frozen = rec.getFrozen() + rec.getAvailable();
            rec.setAvailable(0L).setFrozen(frozen);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long frozen = rec.getFrozen() + point;
            rec.setAvailable(available).setFrozen(frozen);
            this.updatePointRec(type, rec);

        }
        this.pointRecLogService.addLogByRec(type, PointStatus.FREEZE,
                rec, delta, note);
    }

    public void unfreezePoint(String type, PointRecPo rec, long point, String note) {

        long delta = point;
        if (rec.getFrozen() < delta) {
            delta = rec.getFrozen();
            long available = rec.getAvailable() + rec.getFrozen();
            rec.setFrozen(0L).setAvailable(available);
            this.updatePointRec(type, rec);
        } else {
            long frozen = rec.getFrozen() - point;
            long available = rec.getAvailable() + point;
            rec.setAvailable(available).setFrozen(frozen);
            this.updatePointRec(type, rec);

        }
        this.pointRecLogService.addLogByRec(type, PointStatus.UNFREEZE,
                rec, delta, note);
    }

    public void cancelPointRec(String type, PointRecPo rec, String note) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setCancelled(available);
        this.updatePointRec(type, rec);

        this.pointRecLogService.addLogByRec(type, PointStatus.CANCEL,
                rec, available, note);
    }

    public void expirePointRec(String type, PointRecPo rec, String note) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setExpire(available).setEnable(false);
        this.updatePointRec(type, rec);

        this.pointRecLogService.addLogByRec(type, PointStatus.EXPIRE,
                rec, available, note);
    }

    public void updatePointRec(String type, PointRecPo rec) {
        if (rec.getAvailable() == 0L && rec.getFrozen() == 0L) {
            // totally used
            rec.setEnable(false);
        }
        this.pointRecMapper.updatePointRec(TableNameHelper.getPointTableName(type), rec);


    }
}
