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

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.param.PointCancelParam;
import io.github.alphajiang.hyena.model.param.PointIncreaseParam;
import io.github.alphajiang.hyena.model.param.PointOpParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

@AutoConfigureMockMvc
public class TestPointController extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointRecService pointRecService;


    @Before
    public void init() {
        super.init();
    }

    @Test
    public void test_listPoint() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/listPoint")
                .param("type", super.getPointType());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointPo>>() {

        });
        List<PointPo> list = res.getData();
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void test_listPoint_fail_a() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/listPoint")
                .param("type", "invalid_type_test");

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);

        Assert.assertFalse(res.getStatus() == HyenaConstants.RES_CODE_SUCCESS);
    }

    @Test
    public void test_listPointRecord() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/listPointRecord")
                .param("type", super.getPointType());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointRec> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointRec>>() {

        });
        List<PointRec> list = res.getData();
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void test_increase() throws Exception {
        PointIncreaseParam param = new PointIncreaseParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(9876L);
        param.setSeq("gewgewglekjwklehjoipvnbldsalkdjglajd");
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/increase")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_increase_fail() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("{").append("\"uid\":\"").append(super.getUid()).append("\",")
                .append("\"point\":\"").append("abcd").append("\"")
                .append("}");
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/increase")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(buf.toString());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assert.assertEquals(HyenaConstants.RES_CODE_PARAMETER_ERROR, res.getStatus());
    }

    @Test
    public void test_increase_fail_b() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("{").append("\"uid\":\"").append(super.getUid()).append("\",")
                .append("\"point\":\"").append(123).append("\",")
                .append("\"type\":null")
                .append("}");
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/increase")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(buf.toString());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assert.assertEquals(HyenaConstants.RES_CODE_PARAMETER_ERROR, res.getStatus());
    }


    @Test
    public void test_decrease() throws Exception {
        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(1L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/decrease")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_freeze() throws Exception {
        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(1L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/freeze")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_decreaseFrozen() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(9L).setType(super.getPointType()).setUid(super.getUid());
        this.pointUsageFacade.freeze(freeze);

        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(9L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/decreaseFrozen")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }


    @Test
    public void test_unfreeze() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(9L).setType(super.getPointType()).setUid(super.getUid());
        this.pointUsageFacade.freeze(freeze);

        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(9L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/unfreeze")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_cancel() throws Exception {


        ListPointRecParam listParam = new ListPointRecParam();
        listParam.setFrozen(false).setUid(super.getUid()).setType(super.getPointType());
        List<PointRec> recList = this.pointRecService.listPointRec(super.getPointType(), listParam);
        Assert.assertTrue(CollectionUtils.isNotEmpty(recList));
        PointRec rec = recList.iterator().next();


        PointCancelParam param = new PointCancelParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(rec.getAvailable());
        param.setRecId(rec.getId());
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/cancel")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }
}
