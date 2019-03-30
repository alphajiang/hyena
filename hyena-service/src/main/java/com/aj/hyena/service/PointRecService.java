package com.aj.hyena.service;

import com.aj.hyena.mapper.PointRecMapper;
import com.aj.hyena.model.po.PointRecLogPo;
import com.aj.hyena.model.po.PointRecPo;
import com.aj.hyena.model.type.PointRecLogType;
import com.aj.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PointRecService {
    private static final Logger logger = LoggerFactory.getLogger(PointRecService.class);


    @Autowired
    private PointRecMapper pointRecMapper;

    @Autowired
    private PointRecLogService pointRecLogService;

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

        recLog.setPid(pointId).setRecId(rec.getId()).setType(PointRecLogType.INCREASE.code())
                .setDelta(point).setAvailable(point)
                .setUsed(0L).setFrozen(0L).setExpire(0L).setNote(note);
        this.pointRecLogService.addPointRecLog(type, recLog);
    }
}
