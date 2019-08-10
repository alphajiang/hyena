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

import io.github.alphajiang.hyena.ds.mapper.PointLogMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointLogDs {
    private static final Logger logger = LoggerFactory.getLogger(PointLogDs.class);

    @Autowired
    private PointLogMapper pointLogMapper;

    public void addPointLog(String type, PointPo point, long delta, String tag, String extra, List<PointRecLogPo> recLogs) {
        String tableName = TableNameHelper.getPointTableName(type);
        PointLogPo pointLog = new PointLogPo();
        PointRecLogPo pointRecLog = recLogs.get(0);
        pointLog.setUid(point.getUid())
                .setRecLogIds(CollectionUtils.join(recLogs.stream().map(PointRecLogPo::getId).collect(Collectors.toList()), ","))
                .setDelta(delta).setPoint(point.getPoint())
                .setAvailable(point.getAvailable())
                .setUsed(point.getUsed())
                .setFrozen(point.getFrozen())
                .setExpire(point.getExpire())
                .setType(pointRecLog.getType())
                .setExtra(extra);
        if (StringUtils.isNotBlank(tag)) {
            pointLog.setTag(tag);
        } else {
            pointLog.setTag("");
        }
        if (StringUtils.isNotBlank(pointRecLog.getNote())) {
            pointLog.setNote(pointRecLog.getNote());
        } else {
            pointLog.setNote("");
        }
        this.pointLogMapper.addPointLog(tableName, pointLog);
    }

    @Transactional
    public ListResponse<PointLog> listPointLog4Page(ListPointLogParam param) {
        logger.debug("param = {}", param);
        var list = this.listPointLog(param);
        var total = this.countPointLog(param);
        var ret = new ListResponse<>(list, total);
        return ret;
    }

    public List<PointLog> listPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        return this.pointLogMapper.listPointLog(pointTableName, param);
    }

    public long countPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        Long ret = this.pointLogMapper.countPointLog(pointTableName, param);
        return ret == null ? 0L : ret.longValue();
    }
}
