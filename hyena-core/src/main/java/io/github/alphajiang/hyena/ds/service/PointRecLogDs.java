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

import io.github.alphajiang.hyena.ds.mapper.PointRecLogMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointRecLog;
import io.github.alphajiang.hyena.model.param.ListPointRecLogParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointRecLogDs {
    private static final Logger logger = LoggerFactory.getLogger(PointRecLogDs.class);

    @Autowired
    private PointRecLogMapper pointRecLogMapper;

    @Transactional
    public PointRecLogPo addLogByRec(String type, PointRecPo rec, PointLogPo pointLog,
                                     long delta) {

        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setSeqNum(pointLog.getSeqNum()).setRecId(rec.getId()).setType(pointLog.getType())
                .setDelta(delta);
        recLog.setAvailable(rec.getAvailable() == null ? 0L : rec.getAvailable());
        recLog.setUsed(rec.getUsed() == null ? 0L : rec.getUsed());
        recLog.setFrozen(rec.getFrozen() == null ? 0L : rec.getFrozen());
        recLog.setCancelled(rec.getCancelled() == null ? 0L : rec.getCancelled());
        recLog.setExpire(rec.getExpire() == null ? 0L : rec.getExpire());
        recLog.setCost(0L);
        recLog.setSourceType(pointLog.getSourceType()).setOrderType(pointLog.getOrderType())
                .setPayType(pointLog.getPayType());
        recLog.setNote(pointLog.getNote() == null ? "" : pointLog.getNote());
        this.addPointRecLog(type, recLog);
        return recLog;
    }

    @Transactional
    public PointRecLogPo addUsedLogByRec(String type, PointRecPo rec, PointLogPo pointLog,
                                     long delta, long cost) {

        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setSeqNum(pointLog.getSeqNum()).setRecId(rec.getId()).setType(pointLog.getType())
                .setDelta(delta);
        recLog.setAvailable(rec.getAvailable() == null ? 0L : rec.getAvailable());
        recLog.setUsed(rec.getUsed() == null ? 0L : rec.getUsed());
        recLog.setFrozen(rec.getFrozen() == null ? 0L : rec.getFrozen());
        recLog.setCancelled(rec.getCancelled() == null ? 0L : rec.getCancelled());
        recLog.setExpire(rec.getExpire() == null ? 0L : rec.getExpire());
        recLog.setCost(cost);
        recLog.setSourceType(pointLog.getSourceType()).setOrderType(pointLog.getOrderType())
                .setPayType(pointLog.getPayType());
        recLog.setNote(pointLog.getNote() == null ? "" : pointLog.getNote());
        this.addPointRecLog(type, recLog);
        return recLog;
    }

    public PointRecLogPo buildRecLog(PointRecPo rec, PointLogPo pointLog,
                                     long delta) {
        return this.buildRecLog(rec, pointLog, delta, 0L);
    }

    public PointRecLogPo buildRecLog(PointRecPo rec, PointLogPo pointLog,
                            long delta, long cost) {
        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setSeqNum(pointLog.getSeqNum()).setRecId(rec.getId()).setType(pointLog.getType())
                .setDelta(delta);
        recLog.setAvailable(rec.getAvailable() == null ? 0L : rec.getAvailable());
        recLog.setUsed(rec.getUsed() == null ? 0L : rec.getUsed());
        recLog.setFrozen(rec.getFrozen() == null ? 0L : rec.getFrozen());
        recLog.setCancelled(rec.getCancelled() == null ? 0L : rec.getCancelled());
        recLog.setExpire(rec.getExpire() == null ? 0L : rec.getExpire());
        recLog.setCost(cost);
        recLog.setSourceType(pointLog.getSourceType()).setOrderType(pointLog.getOrderType())
                .setPayType(pointLog.getPayType());
        recLog.setNote(pointLog.getNote() == null ? "" : pointLog.getNote());
        return recLog;
    }

    public void addPointRecLog(String type, PointRecLogPo recLog) {
        String tableName = TableNameHelper.getPointRecLogTableName(type);
        this.pointRecLogMapper.addPointRecLog(tableName, recLog);
    }

    public void addPointRecLogs(String type, List<PointRecLogPo> recLogs) {
        String tableName = TableNameHelper.getPointRecLogTableName(type);
        this.pointRecLogMapper.addPointRecLogs(tableName, recLogs);
    }

    @Transactional
    public ListResponse<PointRecLog> listPointRecLog4Page(String type, ListPointRecLogParam param) {
        logger.debug("type = {}, param = {}", type, param);
        var list = this.listPointRecLog(type, param);
        var total = this.countPointRecLog(type, param);
        var ret = new ListResponse<>(list, total);
        return ret;
    }

    public List<PointRecLog> listPointRecLog(String type, ListPointRecLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecLogMapper.listPointRecLog(pointTableName, param);
    }

    public long countPointRecLog(String type, ListPointRecLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        Long ret = this.pointRecLogMapper.countPointRecLog(pointTableName, param);
        return ret == null ? 0L : ret.longValue();
    }
}
