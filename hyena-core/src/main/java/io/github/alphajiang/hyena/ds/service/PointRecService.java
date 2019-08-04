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

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.mapper.PointRecMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointStatus;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ListResponse<PointRec> listPointRec4Page(String type, ListPointRecParam param) {
        var list = this.listPointRec(type, param);
        var total = this.countPointRec(type, param);
        var ret = new ListResponse<>(list, total);
        return ret;
    }

    @Transactional
    public List<PointRec> listPointRec(String type, ListPointRecParam param) {
        logger.debug("type = {}, param = {}", type, param);
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecMapper.listPointRec(pointTableName, param);
    }

    public long countPointRec(String type, ListPointRecParam param) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        Long ret = this.pointRecMapper.countPointRec(pointTableName, param);
        return ret == null ? 0L : ret.longValue();
    }


    /**
     * 增加积分
     *
     * @param param   参数
     * @param pointId 积分ID
     * @return 返回积分记录
     */
    //@Transactional(propagation = Propagation.MANDATORY)   ??????
    @Transactional
    public PointRecPo addPointRec(PointUsage param, long pointId) {
        logger.info("param = {}", param);
        PointRecPo rec = new PointRecPo();
        PointRecLogPo recLog = new PointRecLogPo();
        rec.setPid(pointId).setTotal(param.getPoint()).setAvailable(param.getPoint());
        if (param.getTag() == null) {
            rec.setTag("");
        } else {
            rec.setTag(param.getTag());
        }
        if (StringUtils.isNotBlank(param.getExtra())) {
            rec.setExtra(param.getExtra());
        }
        if (param.getIssueTime() != null) {
            rec.setIssueTime(param.getIssueTime());
        } else {
            rec.setIssueTime(new Date());
        }
        if (param.getExpireTime() != null) {
            rec.setExpireTime(param.getExpireTime());
        }
        String recTableName = TableNameHelper.getPointRecTableName(param.getType());
        this.pointRecMapper.addPointRec(recTableName, rec);


        return rec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PointRecPo decreasePoint(String type, PointRecPo rec, long point, String note) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            long used = rec.getUsed() + rec.getAvailable();
            rec.setAvailable(0L).setUsed(used);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long used = rec.getUsed() + point;
            rec.setAvailable(available).setUsed(used);
            this.updatePointRec(type, rec);

        }
        return rec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PointRecLogPo decreasePointUnfreeze(String type, PointRecPo rec, long point, String note) {

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

        var recLog = this.pointRecLogService.addLogByRec(type, PointStatus.DECREASE,
                rec, delta, note);
        return recLog;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PointRecPo freezePoint(String type, PointRecPo rec, long point, String note) {

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
        return rec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PointRecPo unfreezePoint(String type, PointRecPo rec, long point, String note) {

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
        return rec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelPointRec(String type, PointRecPo rec, String note) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setCancelled(available);
        this.updatePointRec(type, rec);

        this.pointRecLogService.addLogByRec(type, PointStatus.CANCEL,
                rec, available, note);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void expirePointRec(String type, PointRecPo rec, String note) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setExpire(available).setEnable(false);
        this.updatePointRec(type, rec);

        this.pointRecLogService.addLogByRec(type, PointStatus.EXPIRE,
                rec, available, note);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updatePointRec(String type, PointRecPo rec) {
        if (rec.getAvailable() == 0L && rec.getFrozen() == 0L) {
            // totally used
            rec.setEnable(false);
        }
        this.pointRecMapper.updatePointRec(TableNameHelper.getPointTableName(type), rec);


    }

    /**
     * 查询 从start到end间总共增加的积分数量
     *
     * @param type
     * @param uid
     * @param start
     * @param end
     * @return
     */
    public long getIncreasedPoint(String type, String uid, Date start, Date end) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        Long ret = this.pointRecMapper.getIncreasedPoint(pointTableName, uid, start, end);
        return ret == null ? 0L : ret;
    }
}
