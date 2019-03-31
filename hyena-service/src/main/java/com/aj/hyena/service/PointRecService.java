package com.aj.hyena.service;

import com.aj.hyena.mapper.PointRecMapper;
import com.aj.hyena.model.param.ListPointRecParam;
import com.aj.hyena.model.po.PointRecLogPo;
import com.aj.hyena.model.po.PointRecPo;
import com.aj.hyena.model.type.PointRecLogType;
import com.aj.hyena.utils.TableNameHelper;
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


    public List<PointRecPo> listPointRec(String type, ListPointRecParam param) {
        logger.debug("type = {}, param = {}", type, param);
        String pointTableName = TableNameHelper.getPointTableName(type);
        return this.pointRecMapper.listPointRec(pointTableName, param);
    }

    /**
     * 增加积分
     *
     * @param type
     * @param pointId
     * @param point
     * @param tag
     * @param expireTime
     * @param note
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

        recLog.setPid(pointId).setRecId(rec.getId()).setType(PointRecLogType.INCREASE.code())
                .setDelta(point).setAvailable(point)
                .setUsed(0L).setFrozen(0L).setExpire(0L).setNote(note);
        this.pointRecLogService.addPointRecLog(type, recLog);
    }

    public void decreasePoint(String type, PointRecPo rec, long point, String note){

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

        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setRecId(rec.getId()).setType(PointRecLogType.DECREASE.code())
                .setDelta(delta).setAvailable(rec.getAvailable())
                .setUsed(rec.getUsed()).setFrozen(rec.getFrozen()).setExpire(rec.getExpire()).setNote(note);
        this.pointRecLogService.addPointRecLog(type, recLog);
    }

    public void decreasePointUnfreeze(String type, PointRecPo rec, long point, String note){

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

        PointRecLogPo recLog = new PointRecLogPo();
        recLog.setPid(rec.getPid()).setRecId(rec.getId()).setType(PointRecLogType.DECREASE.code())
                .setDelta(delta).setAvailable(rec.getAvailable())
                .setUsed(rec.getUsed()).setFrozen(rec.getFrozen()).setExpire(rec.getExpire()).setNote(note);
        this.pointRecLogService.addPointRecLog(type, recLog);
    }

    public void updatePointRec(String type, PointRecPo rec) {
        if(rec.getAvailable() == 0L && rec.getFrozen() == 0L) {
            // totally used
            rec.setEnable(false);
        }
        this.pointRecMapper.updatePointRec(TableNameHelper.getPointTableName(type), rec);


    }
}
