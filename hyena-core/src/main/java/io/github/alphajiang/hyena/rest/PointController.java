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

package io.github.alphajiang.hyena.rest;

import io.github.alphajiang.hyena.aop.Idempotent;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.ds.service.PointService;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.utils.LoggerHelper;
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
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRecService pointRecService;

    @GetMapping(value = "/listPoint", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointPo> listPoint(HttpServletRequest request,
                                           @RequestParam(defaultValue = "default") String type,
                                           @RequestParam(defaultValue = "0") long start,
                                           @RequestParam(defaultValue = "10") int size) {
        logger.info(LoggerHelper.formatEnterLog(request));
        ListPointParam param = new ListPointParam();
        param.setType(type).setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)))
                .setStart(start).setSize(size);
        var list = this.pointService.listPoint(param);
        ListResponse<PointPo> res = new ListResponse<>(list);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @GetMapping(value = "/listPointRecord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointRec> listPointRecord(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "default") String type,
                                                  @RequestParam(required = false) String uid,
                                                  @RequestParam(required = false) Boolean enable,
                                                  @RequestParam(defaultValue = "0") long start,
                                                  @RequestParam(defaultValue = "10") int size) {
        logger.info(LoggerHelper.formatEnterLog(request));

        ListPointRecParam param = new ListPointRecParam();
        param.setUid(uid);
        param.setEnable(enable).setSorts(List.of(SortParam.as("rec.id", SortOrder.desc)))
                .setStart(start).setSize(size);
        var list = this.pointRecService.listPointRec(type, param);
        ListResponse<PointRec> res = new ListResponse<>(list);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "increase-point")
    @PostMapping(value = "/increase", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> increasePoint(HttpServletRequest request,
                                                 @RequestBody PointIncreaseParam param) {
        logger.info(LoggerHelper.formatEnterLog(request) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        PointPo ret = this.pointUsageFacade.increase(usage);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "decrease-point")
    @PostMapping(value = "/decrease", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> decreasePoint(HttpServletRequest request,
                                                 @RequestBody PointOpParam param) {
        logger.info(LoggerHelper.formatEnterLog(request));
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointPo ret = this.pointUsageFacade.decrease(usage);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @Idempotent(name = "decreaseFrozen-point")
    @PostMapping(value = "/decreaseFrozen", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> decreaseFrozenPoint(HttpServletRequest request,
                                                       @RequestBody PointOpParam param) {
        logger.info(LoggerHelper.formatEnterLog(request));
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointPo ret = this.pointUsageFacade.decreaseFrozen(usage);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @Idempotent(name = "freeze-point")
    @PostMapping(value = "/freeze", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> freezePoint(HttpServletRequest request,
                                               @RequestBody PointOpParam param) {
        logger.info(LoggerHelper.formatEnterLog(request));

        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointPo cusPoint = this.pointUsageFacade.freeze(usage);
        ObjectResponse<PointPo> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "unfreeze-point")
    @PostMapping(value = "/unfreeze", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> unfreezePoint(HttpServletRequest request,
                                                 @RequestBody PointOpParam param) {
        logger.info(LoggerHelper.formatEnterLog(request));

        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointPo cusPoint = this.pointUsageFacade.unfreeze(usage);

        ObjectResponse<PointPo> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "cancel-point")
    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> cancelPoint(HttpServletRequest request,
                                               @RequestBody PointCancelParam param) {
        logger.info(LoggerHelper.formatEnterLog(request));

        PointUsage usage = PointUsageBuilder.fromPointCancelParam(param);
        PointPo cusPoint = this.pointUsageFacade.cancel(usage);

        ObjectResponse<PointPo> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }
}
