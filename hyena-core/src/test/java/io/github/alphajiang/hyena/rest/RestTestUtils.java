package io.github.alphajiang.hyena.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.param.PointFreezeParam;
import io.github.alphajiang.hyena.model.param.PointIncreaseParam;
import io.github.alphajiang.hyena.model.param.PointUnfreezeParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RestTestUtils {


    @Autowired(required = false)
    private MockMvc mockMvc;

    /**
     * 增加积分
     *
     * @param point 增加的数量
     * @throws Exception
     */
    public void increase(HyenaTestBase test, BigDecimal point) throws Exception {
        PointIncreaseParam param = new PointIncreaseParam();
        param.setType(test.getPointType());
        param.setUid(test.getUid());
        param.setSubUid(test.getSubUid());
        if (point != null) {
            param.setPoint(point);
        } else {
            param.setPoint(new BigDecimal("9876.54"));
        }
        param.setSeq(UUID.randomUUID().toString());
        param.setSourceType(1).setOrderType(2).setPayType(3);
        Map<String, Object> extra = new HashMap<>();
        extra.put("aaa", "bbbb");
        extra.put("ccc", 123);
        param.setExtra(JsonUtils.toJsonString(extra));
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/increase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        log.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }


    public ObjectResponse<PointOpResult> freeze(HyenaTestBase test, BigDecimal freezePoint) throws Exception {
        return this.freeze(test, freezePoint, null);
    }

    /**
     * 冻结积分
     *
     * @param freezePoint 冻结的数量
     * @throws Exception
     */
    public ObjectResponse<PointOpResult> freeze(HyenaTestBase test,
                                                BigDecimal freezePoint,
                                                String orderNo) throws Exception {
        PointFreezeParam param = new PointFreezeParam();
        param.setType(test.getPointType());
        param.setUid(test.getUid());
        param.setSubUid(test.getSubUid());
        if (freezePoint != null) {
            param.setPoint(freezePoint);
        } else {
            param.setPoint(new BigDecimal("1.23"));
        }
        if (StringUtils.isNotBlank(orderNo)) {
            param.setOrderNo(orderNo);
        }
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        log.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        return res;
    }

    public ObjectResponse<PointOpResult> unfreeze(HyenaTestBase test, BigDecimal unfreezePoint) throws Exception {
        return this.unfreeze(test, unfreezePoint, null);
    }

    public ObjectResponse<PointOpResult> unfreeze(HyenaTestBase test, BigDecimal unfreezePoint, String orderNo) throws Exception {
        PointUnfreezeParam param = new PointUnfreezeParam();
        param.setType(test.getPointType());
        param.setUid(test.getUid());
        param.setSubUid(test.getSubUid());
        if (unfreezePoint != null) {
            param.setPoint(unfreezePoint);
        } else {
            param.setPoint(BigDecimal.valueOf(9L));
        }
        if (StringUtils.isNotBlank(orderNo)) {
            param.setUnfreezeByOrderNo(true);
            param.setOrderNo(orderNo);
        }
        RequestBuilder builder = MockMvcRequestBuilders.post("/hyena/point/unfreeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        log.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        return res;
    }
}
