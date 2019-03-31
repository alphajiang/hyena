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

package com.aj.hyena.rest;

import com.aj.hyena.model.base.ListResponse;
import com.aj.hyena.model.base.ObjectResponse;
import com.aj.hyena.model.param.ListPointRecParam;
import com.aj.hyena.model.param.SortParam;
import com.aj.hyena.model.po.PointRecPo;
import com.aj.hyena.model.type.SortOrder;
import com.aj.hyena.service.PointRecService;
import com.aj.hyena.service.PointService;
import com.aj.hyena.utils.LoggerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/hyena/point")
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private PointService cusPointService;

    @Autowired
    private PointRecService pointRecService;

    @GetMapping(value = "/listPointRecord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointRecPo> listPointRecord(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "default") String type,
                                                    @RequestParam(required = false) String cusId,
                                                    @RequestParam(defaultValue = "0") long start,
                                                    @RequestParam(defaultValue = "10") int size) {
        logger.info(LoggerHelper.formatEnterLog(request));

        ListPointRecParam param = new ListPointRecParam();
        param.setCusId(cusId).setAvailable(true);
        param.setSorts(List.of(SortParam.as("rec.id", SortOrder.desc)))
                .setStart(start).setSize(size);
        //param.getSorts().add();
        var list = this.pointRecService.listPointRec(type, param);
        ListResponse<PointRecPo> res = new ListResponse<>(list);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @PostMapping(value = "/increase", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<Long> increasePoint(HttpServletRequest request,
                                              @RequestParam(defaultValue = "default") String type,
                                              @RequestParam String cusId,
                                              @RequestParam long point) {
        logger.info(LoggerHelper.formatEnterLog(request));

        var cusPoint = this.cusPointService.increasePoint(type, cusId, point);
        ObjectResponse<Long> res = new ObjectResponse<>(cusPoint.getPoint());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @PostMapping(value = "/decrease", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<Long> decreasePoint(HttpServletRequest request,
                                              @RequestParam(defaultValue = "default") String type,
                                              @RequestParam String cusId,
                                              @RequestParam long point,
                                              @RequestParam(defaultValue = "false") boolean unfreeze,
                                              @RequestParam(defaultValue = "") String note) {
        logger.info(LoggerHelper.formatEnterLog(request));

        Long cusPoint = null;
        if (unfreeze) {
            cusPoint = this.cusPointService.decreasePointUnfreeze(type, cusId, point, note);
        } else {
            cusPoint = this.cusPointService.decreasePoint(type, cusId, point, note);
        }
        ObjectResponse<Long> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }
}
