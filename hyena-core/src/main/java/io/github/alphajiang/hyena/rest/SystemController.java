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
import io.github.alphajiang.hyena.ds.service.PointTableService;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
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

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestController
@Api(value = "系统设置接口", tags = "系统")
@RequestMapping("/hyena/system")
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private PointTableService pointTableService;

    @ApiOperation(value = "获取积分类型列表")
    @GetMapping(value = "/listPointType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<String> listPointType(HttpServletRequest request) {
        logger.debug(LoggerHelper.formatEnterLog(request));
        var list = this.pointTableService.listTable();
        list = list.stream()
                .map(o -> StringUtils.replaceFirst(o, HyenaConstants.PREFIX_POINT_TABLE_NAME))
                .collect(Collectors.toList());
        ListResponse<String> res = new ListResponse<>(list);
        logger.debug(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "新增积分类型")
    @PostMapping(value = "/addPointType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse addPointType(HttpServletRequest request,
                                     @ApiParam(value = "积分类型", example = "score") @RequestParam(name = "name", required = true) String name) {
        logger.info(LoggerHelper.formatEnterLog(request));
        this.pointTableService.getOrCreateTable(name);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return BaseResponse.success();
    }
}
