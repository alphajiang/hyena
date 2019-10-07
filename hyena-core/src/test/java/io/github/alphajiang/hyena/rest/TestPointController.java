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
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.dto.PointRecLog;
import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DateUtils;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfigureMockMvc
public class TestPointController extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointRecDs pointRecDs;


    @Before
    public void init() {
        super.init();
    }

    @Test
    public void test_getPoint() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/getPoint")
                .param("type", super.getPointType())
                .param("uid", super.getUid());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo ret = res.getData();
        Assert.assertNotNull(ret);
    }

    @Test
    public void test_listPoint() throws Exception {

        ListPointParam param = new ListPointParam();
        param.setType(super.getPointType());
        param.setUidList(List.of(super.getUid()));
        param.setStart(0L).setSize(10);


        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/listPoint")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointPo>>() {

        });
        List<PointPo> list = res.getData();
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void test_listPoint_fail_a() throws Exception {


        ListPointParam param = new ListPointParam();
        param.setType("invalid_type_test");
        param.setStart(0L).setSize(10);

        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/listPoint")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);

        Assert.assertFalse(res.getStatus() == HyenaConstants.RES_CODE_SUCCESS);
    }

    @Test
    public void test_listPointLog() throws Exception {
        Thread.sleep(100L);
        ListPointLogParam param = new ListPointLogParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setLogTypes(List.of(PointOpType.INCREASE.code()));
        param.setSourceTypes(List.of(super.getSourceType(), 2, 3));
        param.setOrderTypes(List.of(super.getOrderType(), 4, 5, 6));
        param.setPayTypes(List.of(super.getPayType(), 7, 8, 9));
        param.setStart(0L).setSize(10);

        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/listPointLog")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointLog> res = JsonUtils.fromJson(resBody, new TypeReference<>() {

        });
        List<PointLog> list = res.getData();
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(res.getTotal() > 0L);
    }

    @Test
    public void test_listPointRecord() throws Exception {
        Thread.sleep(100L);
        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/listPointRecord")
                .param("type", super.getPointType())
                .param("tag", super.getTag());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointRec> res = JsonUtils.fromJson(resBody, new TypeReference<>() {

        });
        List<PointRec> list = res.getData();
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(res.getTotal() > 0L);
    }

    @Test
    public void test_listPointRecordLog() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/listPointRecordLog")
                .param("type", super.getPointType())
                .param("tag", super.getTag());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointRecLog> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointRecLog>>() {

        });
        List<PointRecLog> list = res.getData();
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(res.getTotal() > 0L);
    }


    @Test
    public void test_increase() throws Exception {
        PointIncreaseParam param = new PointIncreaseParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(9876L);
        param.setSeq("gewgewglekjwklehjoipvnbldsalkdjglajd");
        param.setSourceType(1).setOrderType(2).setPayType(3);
        Map<String, Object> extra = new HashMap<>();
        extra.put("aaa", "bbbb");
        extra.put("ccc", 123);
        param.setExtra(JsonUtils.toJsonString(extra));
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/increase")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

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
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_freeze() throws Exception {
        PointFreezeParam param = new PointFreezeParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(1L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/freeze")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

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
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    /**
     * 消费积分, 同时解冻积分
     */
    @Test
    public void test_decreaseFrozen_unfreeze() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(14L).setType(super.getPointType()).setUid(super.getUid());
        this.pointUsageFacade.freeze(freeze);

        PointDecreaseParam param = new PointDecreaseParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setUnfreezePoint(5L); // 要做解冻的部分
        param.setPoint(9L); // 要消费的部分
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/decreaseFrozen")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }


    @Test
    public void test_unfreeze() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(9L).setType(super.getPointType()).setUid(super.getUid());
        this.pointUsageFacade.freeze(freeze);

        PointUnfreezeParam param = new PointUnfreezeParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setPoint(9L);
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/unfreeze")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }


    @Test
    public void test_refund() throws Exception {
        PointRefundParam param = new PointRefundParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setCost(5L); // 退款部分的成本
        param.setPoint(10L); // 退款的部分
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/refund")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_cancel() throws Exception {


        ListPointRecParam listParam = new ListPointRecParam();
        listParam.setFrozen(false).setUid(super.getUid()).setType(super.getPointType());
        Thread.sleep(100L);
        List<PointRec> recList = this.pointRecDs.listPointRec(super.getPointType(), listParam);
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
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_getIncreasedPoint() throws Exception {
        Thread.sleep(100L);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);

        RequestBuilder builder = MockMvcRequestBuilders.get("/hyena/point/getIncreasedPoint")
                .param("type", super.getPointType())
                .param("uid", super.getUid())
                .param("start", DateUtils.toYyyyMmDdHhMmSs(start))
                .param("end", DateUtils.toYyyyMmDdHhMmSs(end));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<Long> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<Long>>() {

        });
        Long result = res.getData();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.longValue() > 0L);
    }
}
