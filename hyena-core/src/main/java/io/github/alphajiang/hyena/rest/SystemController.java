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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.cache.HyenaCacheFactory;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.flow.QueueMonitor;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.vo.QueueInfo;
import io.github.alphajiang.hyena.utils.LoggerHelper;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(value = "系统设置接口", tags = "系统")
@RequestMapping(value = "/hyena/system", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Autowired
    private HyenaCacheFactory hyenaCacheFactory;

    @Autowired
    private QueueMonitor queueMonitor;

    @ApiOperation(value = "获取积分类型列表")
    @GetMapping(value = "/listPointType")
    public ListResponse<String> listPointType(ServerWebExchange exh) {
        logger.debug(LoggerHelper.formatEnterLog(exh));
        var list = this.pointTableDs.listTable();
        list = list.stream()
                .map(o -> StringUtils.replaceFirst(o, HyenaConstants.PREFIX_POINT_TABLE_NAME))
                .collect(Collectors.toList());
        ListResponse<String> res = new ListResponse<>(list);
        logger.debug(LoggerHelper.formatLeaveLog(exh));
        return res;
    }

    @ApiOperation(value = "新增积分类型")
    @PostMapping(value = "/addPointType")
    public BaseResponse addPointType(ServerWebExchange exh,
                                     @ApiParam(value = "积分类型", example = "score") @RequestParam(name = "name", required = true) String name) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        this.pointTableDs.getOrCreateTable(name);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return BaseResponse.success();
    }


    @ApiOperation(value = "获取缓存信息")
    @GetMapping(value = "/dumpMemCache")
    public ListResponse<PointCache> dumpMemCache(ServerWebExchange exh) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        List<PointCache> list = new ArrayList<>();
        list.addAll(hyenaCacheFactory.getPointCacheService().dump());
        ListResponse<PointCache> ret = new ListResponse<>(list, list.size());
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return ret;
    }

    @ApiOperation(value = "获取队列信息")
    @GetMapping(value = "/dumpQueue")
    public ListResponse<QueueInfo> dumpQueue(ServerWebExchange exh) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        List<QueueInfo> list = queueMonitor.dump();
        ListResponse<QueueInfo> ret = new ListResponse<>(list);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return ret;
    }

    @ApiOperation(value = "分析积分日志")
    @GetMapping(value = "/analysePointLog")
    public BaseResponse analysePointLog(
            ServerWebExchange exh,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(required = true) String type,
            @ApiParam(value = "用户标识", example = "abcd") @RequestParam(required = true) String uid,
            @ApiParam(value = "用户副标识", example = "efgh") @RequestParam(required = true) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(exh));
        this.pointLogDs.analyseLog(type, uid, subUid);
        logger.info(LoggerHelper.formatLeaveLog(exh));
        return BaseResponse.success();
    }
}
