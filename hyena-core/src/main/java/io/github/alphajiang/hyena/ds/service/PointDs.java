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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.ds.mapper.PointMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.param.BaseListParam;
import io.github.alphajiang.hyena.model.param.ListPointParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.vo.PointVo;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class PointDs {
    private static final Logger logger = LoggerFactory.getLogger(PointDs.class);

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private PointTableDs pointTableDs;


//    @Autowired
//    private PointTableService cusPointTableService;
//
//    @Autowired
//    private PointRecService pointRecService;


    public PointPo getCusPoint(String type, String uid, String subUid, boolean lock) {
        String tableName = TableNameHelper.getPointTableName(type);
        PointPo point = this.pointMapper.getCusPointByUid(tableName, uid, subUid, lock);
        if (lock && point != null) {
            point = this.pointMapper.getCusPoint(tableName, point.getId(), lock);
        }
        return point;
    }

    public boolean addPoint(String type, PointPo point) {
        if (point.getSubUid() == null) {
            point.setSubUid("");
        }
        if (point.getName() == null) {
            point.setName("");
        }
        String tableName = TableNameHelper.getPointTableName(type);

        Integer ret = this.pointMapper.addPoint(tableName, point);
        return ret != null && ret.intValue() > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchUpdate(String type, List<PointPo> pointList) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        this.pointMapper.batchUpdate(pointTableName, pointList);
    }

    public List<PointPo> listPoint(ListPointParam param) {
        logger.debug("param = {}", param);
        if (param.getSize() == null) {
            param.setSize(999);
        }
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        HyenaAssert.isTrue(pointTableDs.isTableExists(pointTableName), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "type not exist");
        return this.pointMapper.listPoint(pointTableName, param);
    }

    public ListResponse<PointPo> listPoint4Page(ListPointParam param) {
        logger.debug("param = {}", param);
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        HyenaAssert.isTrue(pointTableDs.isTableExists(pointTableName), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "type not exist");
        var list = this.pointMapper.listPoint(pointTableName, param);
        var total = this.pointMapper.countPoint(pointTableName, param);
        ListResponse<PointPo> res = new ListResponse<>(list, total);
        return res;
    }

    public void disableAccount(String type, String uid, String subUid) {
        this.pointMapper.disableAccount(TableNameHelper.getPointTableName(type), uid, subUid);
    }

    /**
     * pid 和 uid 必须要有一个不为null
     *
     * @param type   积分类型
     * @param pid    积分ID
     * @param uid    用户uid
     * @param subUid 用户子帐号
     * @return 用户积分数据
     */
    public PointVo getPointVo(@NonNull String type,
                              Long pid,
                              String uid, String subUid) {
        if (pid == null && uid == null) {
            throw new HyenaParameterException("invalid parameter");
        }
        String pointTableName = TableNameHelper.getPointTableName(type);
        try {
            return this.pointMapper.getPointVo(pointTableName, pid, uid, subUid);
        } catch (Exception ex) {
            if (ex instanceof BadSqlGrammarException) {
                String rex = "Table '(\\w+.\\w+)' doesn't exist";
                if (Pattern.compile(rex).matcher(ex.getMessage()).find()) {
                    logger.warn("point table does not create yet. type = {}, pid = {}, uid = {}, subUid = {}",
                            type, pid, uid, subUid);
                    // point table not create yet, return an all zero point result
                    PointVo ret = new PointVo();
                    PointPo.buildPointPo(ret);
                    ret.setUid(uid)
                            .setSubUid(subUid)
                            .setId(pid);
                    return ret;
                } else {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
    }

    public boolean update(String type, PointPo point) {
        int ret = this.pointMapper.updateCusPoint(TableNameHelper.getPointTableName(type), point);
        return ret > 0;
    }


    public List<PointPo> listExpirePoint(BaseListParam param) {
        logger.debug("param = {}", param);
        if (param.getSize() == null) {
            param.setSize(999);
        }
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        HyenaAssert.isTrue(pointTableDs.isTableExists(pointTableName), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "type not exist");
        return this.pointMapper.listExpirePoint(pointTableName, param);
    }
}
