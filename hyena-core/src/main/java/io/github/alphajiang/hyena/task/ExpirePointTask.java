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

package io.github.alphajiang.hyena.task;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ExpirePointTask {


    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 30 * 1000)  // every 1 hour
    public void expirePointTask() {
        log.debug(">>");

        List<String> tables = this.pointTableDs.listTable();
        tables.forEach(t -> {
            String type = t.replaceFirst(HyenaConstants.PREFIX_POINT_TABLE_NAME, "");
            this.expirePointByType(type);
        });
        log.debug("<<");
    }

    private void expirePointByType(String type) {

        ListPointRecParam param = new ListPointRecParam();
        param.setFrozen(false).setExpireTime(new Date()).setType(type).setEnable(true);
        List<PointRecDto> pointRecList = this.pointRecDs.listPointRec(param);

        pointRecList.stream()
                .filter(rec -> rec.getAvailable().compareTo(DecimalUtils.ZERO) > 0)
                .forEach(rec -> {
                    try {
                        PointUsage usage = new PointUsage();

                        usage.setUid(rec.getUid()).setPoint(rec.getAvailable())
                                .setType(type).setNote("expire").setRecId(rec.getId());
                        this.pointUsageFacade.expire(usage);
                    } catch (Exception e) {
                        log.error("rec = {}, error = {}", rec, e.getMessage(), e);
                    }
                });
    }
}
