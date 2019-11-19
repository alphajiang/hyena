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
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.param.ListPointRecLogParam;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PointRecDs {
    private static final Logger logger = LoggerFactory.getLogger(PointRecDs.class);


    @Autowired
    private PointRecMapper pointRecMapper;


    @Autowired
    private PointRecLogDs pointRecLogDs;

    public void batchInsert(String type, List<PointRecPo> recList) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.pointRecMapper.batchInsert(pointTableName, recList);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchUpdate(String type, List<PointRecPo> recList) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.pointRecMapper.batchUpdate(pointTableName, recList);
    }

    public PointRecPo getById(String type, long id, boolean lock) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecMapper.getById(pointTableName, id, lock);
    }

    @Transactional
    public ListResponse<PointRecDto> listPointRec4Page(ListPointRecParam param) {
        var list = this.listPointRec(param);
        var total = this.countPointRec(param);
        if (Boolean.TRUE.equals(param.getFetchRecLogs())) {
            List<Long> recIds = list.stream().map(PointRecDto::getId).collect(Collectors.toList());
            ListPointRecLogParam recLogParam = new ListPointRecLogParam();
            recLogParam.setRecIdList(recIds);
            List<PointRecLogDto> recLogList = this.pointRecLogDs.listPointRecLog(param.getType(), recLogParam);
            Map<Long, List<PointRecLogDto>> map = recLogList.stream().collect(Collectors.groupingBy(PointRecLogDto::getRecId, Collectors.toList()));
            list.stream().forEach(rec -> {
                rec.setRecLogs(map.get(rec.getId()));
            });
        }
        var ret = new ListResponse<>(list, total);
        return ret;
    }


    @Transactional
    public List<PointRecDto> listPointRec(ListPointRecParam param) {
        logger.debug(" param = {}", param);
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        return this.pointRecMapper.listPointRec(pointTableName, param);
    }

    public long countPointRec(ListPointRecParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
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
        rec.setPid(pid).setSeqNum(seqNum)
                .setTotal(param.getPoint())
                .setAvailable(param.getPoint())
                .setFrozen(0L)
                .setUsed(0L)
                .setRefundCost(0L)
                .setFrozenCost(0L)
                .setUsedCost(0L)
                .setRefund(0L)
                .setRefundCost(0L)
                .setCancelled(0L)
                .setExpire(0L)
                .setEnable(true)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
        if (param.getOrderNo() == null) {
            rec.setOrderNo("");
        } else {
            rec.setOrderNo(param.getOrderNo());
        }
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


//    public PointRecPo buildPointRec(PointUsage param, long pid, long seqNum) {
//        logger.info("param = {}", param);
//        PointRecPo rec = new PointRecPo();
//        rec.setPid(pid).setSeqNum(seqNum).setTotal(param.getPoint())
//                .setAvailable(param.getPoint())
//                .setRefundCost(0L)
//                .setFrozenCost(0L)
//                .setUsedCost(0L)
//                .setOrderNo(param.getOrderNo());
//        if (param.getCost() != null && param.getCost() > 0L) {
//            rec.setTotalCost(param.getCost());
//        } else {
//            rec.setTotalCost(0L);
//        }
//        if (param.getTag() == null) {
//            rec.setTag("");
//        } else {
//            rec.setTag(param.getTag());
//        }
//        if (StringUtils.isNotBlank(param.getExtra())) {
//            rec.setExtra(param.getExtra());
//        }
//        if (param.getIssueTime() != null) {
//            rec.setIssueTime(param.getIssueTime());
//        } else {
//            rec.setIssueTime(new Date());
//        }
//        if (param.getExpireTime() != null) {
//            rec.setExpireTime(param.getExpireTime());
//        }
//        rec.setSourceType(param.getSourceType())
//                .setOrderType(param.getOrderType())
//                .setPayType(param.getPayType());
//        // String recTableName = TableNameHelper.getPointRecTableName(param.getType());
//        //this.pointRecMapper.addPointRec(recTableName, rec);
//
//
//        return rec;
//    }


    @Transactional
    public PointRecPo expirePointRec(PointRecPo rec) {
        long available = rec.getAvailable();
        rec.setAvailable(0L).setExpire(available).setEnable(false);
        return rec;
    }


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


}
