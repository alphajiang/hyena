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
import io.github.alphajiang.hyena.ds.mapper.PointLogMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.param.SortParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.model.vo.PointLogBi;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Service
public class PointLogDs {
    private static final Logger logger = LoggerFactory.getLogger(PointLogDs.class);

    @Autowired
    private PointLogMapper pointLogMapper;

    @Autowired
    private PointTableDs pointTableDs;


    public void addPointLog(@NonNull String type, @NotNull PointLogPo pointLog) {
        String tableName = TableNameHelper.getPointTableName(type);

        this.pointLogMapper.addPointLog(tableName, pointLog);
    }

    public void batchInsert(@NonNull String type, @NotNull List<PointLogPo> pointLogList) {
        String tableName = TableNameHelper.getPointTableName(type);
        this.pointLogMapper.batchInsert(tableName, pointLogList);
    }


    @Transactional
    public ListResponse<PointLogDto> listPointLog4Page(ListPointLogParam param) {
        logger.debug("param = {}", param);
//        if (param.getCreateTimeFilter() != null && param.getCreateTimeFilter().getEndTime() != null) {
//            param.getCreateTimeFilter().getEndTime();
//        }
        if (param.getSize() == null) {
            param.setSize(999);
        }
        var list = this.listPointLog(param);
        var total = this.countPointLog(param);
        var ret = new ListResponse<>(list, total);
        return ret;
    }

    public List<PointLogDto> listPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        HyenaAssert.isTrue(pointTableDs.isTableExists(pointTableName), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "type not exist");
        return this.pointLogMapper.listPointLog(pointTableName, param);
    }

    public long countPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        Long ret = this.pointLogMapper.countPointLog(pointTableName, param);
        return ret == null ? 0L : ret.longValue();
    }

    public List<PointLogBi> listPointLogBi(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        return this.pointLogMapper.listPointLogBi(pointTableName, param);
    }

    public void analyseLog(String type, String uid, String subUid) {
        String pointTableName = TableNameHelper.getPointTableName(type);
        long start = 0;
        int size = 1000;
        SortParam sort = new SortParam();
        sort.setColumns(List.of("id")).setOrder(SortOrder.asc);
        ListPointLogParam param = new ListPointLogParam();
        param.setUid(uid)
                .setSubUid(subUid)
                .setType(type)
                .setStart(start)
                .setSize(size)
                .setEnable(true)
                .setSorts(List.of(sort));
        int retSize = size;
        PointLogDto last = null;
        while (retSize == size) {
            List<PointLogDto> retList = this.listPointLog(param);
            if (CollectionUtils.isEmpty(retList)) {
                return;
            }
            retSize = retList.size();
            logger.info("type = {}, start = {}, size = {}, retSize = {}",
                    type, param.getStart(), param.getSize(), retSize);
            for (PointLogDto lg : retList) {
                if (last == null) {
                    last = lg;
                    continue;
                }
                this.analyseLog(pointTableName, last, lg);
                last = lg;
            }
            param.setStart(param.getStart() + size);
        }
    }

    private void analyseLog(String pointTableName, PointLogDto prev, PointLogDto cur) {
        boolean update = false;
        if (cur.getType() == null && Boolean.FALSE.equals(cur.getAbnormal())) {
            cur.setAbnormal(true);
            update = true;
        } else if (PointOpType.INCREASE.code() == cur.getType()) {
            update = this.analyseIncreaseLog(prev, cur);
        } else if (PointOpType.DECREASE.code() == cur.getType()) {
            update = this.analyseDecreaseLog(prev, cur);
        } else if (PointOpType.FREEZE.code() == cur.getType()) {
            update = this.analyseFreezeLog(prev, cur);
        } else if (PointOpType.UNFREEZE.code() == cur.getType()) {
            update = this.analyseUnfreezeLog(prev, cur);
        } else if (PointOpType.EXPIRE.code() == cur.getType()) {
            update = this.analyseExpireLog(prev, cur);
        } else if (PointOpType.REFUND.code() == cur.getType()) {
            update = this.analyseRefundLog(prev, cur);
        }
        if (Boolean.TRUE.equals(cur.getAbnormal()) && !update) {
            cur.setAbnormal(false);
            update = true;
        }
        if (update) {
            if (Boolean.TRUE.equals(cur.getAbnormal())) {
                logger.warn("set abnormal = true. pointLog = {}", cur);
            }
            this.pointLogMapper.updateAbnormal(pointTableName, cur.getId(), cur.getAbnormal());
        }
    }

    private boolean analyseIncreaseLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().add(cur.getDelta()).equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().add(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().add(cur.getDeltaCost()).equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }

    private boolean analyseDecreaseLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().subtract(cur.getDelta()).equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().subtract(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().add(cur.getDelta()).equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().subtract(cur.getDeltaCost()).equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }

    private boolean analyseFreezeLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().subtract(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().add(cur.getDelta()).equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().add(cur.getDeltaCost()).equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }

    private boolean analyseUnfreezeLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().add(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().subtract(cur.getDelta()).equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().subtract(cur.getDeltaCost()).equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }

    private boolean analyseExpireLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().subtract(cur.getDelta()).equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().subtract(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().add(cur.getDelta()).equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().subtract(cur.getDelta()).equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }

    private boolean analyseRefundLog(PointLogDto prev, PointLogDto cur) {
        if (!prev.getPoint().subtract(cur.getDelta()).equals(cur.getPoint())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getAvailable().subtract(cur.getDelta()).equals(cur.getAvailable())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getUsed().equals(cur.getUsed())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozen().equals(cur.getFrozen())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getRefund().add(cur.getDelta()).equals(cur.getRefund())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getExpire().equals(cur.getExpire())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getCost().subtract(cur.getDelta()).equals(cur.getCost())) {
            cur.setAbnormal(true);
            return true;
        } else if (!prev.getFrozenCost().equals(cur.getFrozenCost())) {
            cur.setAbnormal(true);
            return true;
        }
        return false;
    }
}
