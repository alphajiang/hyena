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
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
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
public class PointRecDs {
    private static final Logger logger = LoggerFactory.getLogger(PointRecDs.class);


    @Autowired
    private PointRecMapper pointRecMapper;


    @Autowired
    private PointRecLogDs pointRecLogDs;

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
     * @param param 参数
     * @param pid   当前积分对象ID
     * @return 返回积分记录
     */
    @Transactional
    public PointRecPo addPointRec(PointUsage param, long pid, long seqNum) {
        logger.info("param = {}", param);
        PointRecPo rec = new PointRecPo();
        rec.setPid(pid).setSeqNum(seqNum).setTotal(param.getPoint())
                .setAvailable(param.getPoint())
                .setFrozenCost(0L)
                .setUsedCost(0L)
                .setOrderNo(param.getOrderNo());
        if (param.getCost() != null && param.getCost() > 0L) {
            rec.setTotalCost(param.getCost());
        } else {
            rec.setTotalCost(0L);
        }
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
        rec.setSourceType(param.getSourceType())
                .setOrderType(param.getOrderType())
                .setPayType(param.getPayType());
        String recTableName = TableNameHelper.getPointRecTableName(param.getType());
        this.pointRecMapper.addPointRec(recTableName, rec);


        return rec;
    }

    @Transactional
    public PointRecPo decreasePoint(String type, PointRecPo rec, long point, long cost) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            long used = rec.getUsed() + rec.getAvailable();
            rec.setAvailable(0L).setUsed(used).setUsedCost(rec.getUsedCost() + cost);

            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long used = rec.getUsed() + point;
            rec.setAvailable(available).setUsed(used).setUsedCost(rec.getUsedCost() + cost);
            this.updatePointRec(type, rec);

        }
        return rec;
    }


    @Transactional
    public PointRecPo freezePoint(String type, PointRecPo rec, long point, long deltaCost) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            long frozen = rec.getFrozen() + rec.getAvailable();
            rec.setAvailable(0L).setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost() + deltaCost);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long frozen = rec.getFrozen() + point;
            rec.setAvailable(available).setFrozen(frozen)
                    .setFrozenCost(rec.getFrozenCost() + deltaCost);
            this.updatePointRec(type, rec);

        }
        return rec;
    }

    @Transactional
    public PointRecPo unfreezePoint(String type, PointRecPo rec, long point) {

        long delta = point;
        if (rec.getFrozen() < delta) {
            long available = rec.getAvailable() + rec.getFrozen();
            rec.setFrozen(0L).setAvailable(available);
            this.updatePointRec(type, rec);
        } else {
            long frozen = rec.getFrozen() - point;
            long available = rec.getAvailable() + point;
            rec.setAvailable(available).setFrozen(frozen);
            this.updatePointRec(type, rec);

        }
        return rec;
    }

    @Transactional
    public PointRecPo cancelPoint(String type, PointRecPo rec, long point) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            long canceled = rec.getCancelled() + rec.getAvailable();
            rec.setAvailable(0L).setCancelled(canceled);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long canceled = rec.getCancelled() + point;
            rec.setAvailable(available).setCancelled(canceled);
            this.updatePointRec(type, rec);

        }
        return rec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelPointRec(String type, PointRecPo rec, PointLogPo pointLog) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setCancelled(available);
        this.updatePointRec(type, rec);

        long deltaCost = rec.getTotalCost() - rec.getUsedCost() - rec.getFrozenCost();
        this.pointRecLogDs.addLogByRec(type, rec, pointLog, available, deltaCost);
    }

    @Transactional
    public void expirePointRec(String type, PointRecPo rec, PointLogPo pointLog) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setExpire(available).setEnable(false);
        this.updatePointRec(type, rec);
        long deltaCost = rec.getTotalCost() - rec.getUsedCost() - rec.getFrozenCost();
        this.pointRecLogDs.addLogByRec(type, rec, pointLog, available, deltaCost);
    }

    @Transactional
    public PointRecPo refundPoint(String type, PointRecPo rec, long point, long cost) {

        long delta = point;
        if (rec.getAvailable() < delta) {
            long refund = rec.getRefund() + rec.getAvailable();
            rec.setAvailable(0L).setRefund(refund).setUsedCost(rec.getUsedCost() + cost);
            this.updatePointRec(type, rec);
        } else {
            long available = rec.getAvailable() - point;
            long refund = rec.getRefund() + point;
            rec.setAvailable(available).setRefund(refund).setUsedCost(rec.getUsedCost() + cost);
            this.updatePointRec(type, rec);

        }
        return rec;
    }

//    @Transactional
//    public void refundPointRec(String type, PointRecPo rec, PointLogPo pointLog) {
////        PointRecPo rec = this.getById(usage.getType(), usage.getRecId(), true);
////        if(rec == null){
////            throw new HyenaParameterException("invalid parameter");
////        }
//        long available = rec.getAvailable();
//        rec.setAvailable(0L)
//                .setRefund(rec.getAvailable())
//                .setId(rec.getId());
//        this.updatePointRec(type, rec);
//        this.pointRecLogDs.addLogByRec(type, rec, pointLog, available);
//    }

    @Transactional
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
     * @param type  积分类型
     * @param uid   账户ID
     * @param start 开始时间
     * @param end   结束时间
     * @return 返回增长的积分数量
     */
    public long getIncreasedPoint(String type, String uid, Date start, Date end) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        Long ret = this.pointRecMapper.getIncreasedPoint(pointTableName, uid, start, end);
        return ret == null ? 0L : ret;
    }

    /**
     * 计算delta(变动)部分在积分块中的所占成本
     *
     * @param rec   积分块
     * @param delta 变动部分
     * @return 所占成本
     */
    public long accountCost(PointRecPo rec, long delta) {
        long cost = 0L;
        if (rec.getTotalCost() == null || rec.getTotalCost() < 1L) { // 积分块没有成本时直接返回0
            return cost;
        }
        if (rec.getAvailable() <= delta) { // 不够抵扣时返回剩余的全部
            cost = rec.getTotalCost() - rec.getUsedCost() - rec.getFrozenCost();
        } else { // 按比例计算
            cost = delta * rec.getTotalCost() / rec.getTotal();
        }
        return cost;
    }
}
