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

package io.github.alphajiang.hyena;

import io.github.alphajiang.hyena.biz.point.PSession;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.ds.service.SysPropertyDs;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.IdGenerator;
import io.github.alphajiang.hyena.utils.JsonUtils;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = HyenaTestMain.class)
@AutoConfigureWebTestClient
//@Transactional
public abstract class HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(HyenaTestBase.class);

    @Autowired
    private SysPropertyDs sysPropertyDs;

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Getter
    @Autowired
    private IdGenerator idGenerator;

    private String pointType;

    private String tag;

    private String uid;
    private String subUid;

    private Integer sourceType = 1;
    private Integer orderType = 11;
    private Integer payType = 21;

    private List<String> orderNoList;

    private PointUsage initialPointUsage;

    private PointPo userPoint;

    public HyenaTestBase() {
        String random = UUID.randomUUID().toString().replace("-", "");
        this.pointType = random.substring(0, 6);
        this.uid = random.substring(7, 12);
        this.subUid = random.substring(3, 7);
        this.tag = random.substring(13, 16);
        Map<String, Object> extra = new HashMap<>();
        extra.put("aaa", "bbbb");
        extra.put("ccc", 123L);
        this.orderNoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.orderNoList.add(UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        }
        this.initialPointUsage = new PointUsage();
        this.initialPointUsage.setType(this.pointType).setTag(tag)
                .setUid(this.uid).setSubUid(this.subUid)
                .setPoint(BigDecimal.valueOf(100L).setScale(DecimalUtils.SCALE_2))
                .setCost(BigDecimal.valueOf(50L).setScale(DecimalUtils.SCALE_2))
                .setSourceType(sourceType).setOrderType(orderType).setPayType(payType)
                .setExtra(JsonUtils.toJsonString(extra));
    }


    public void init() {
        sysPropertyDs.createSysPropertyTable();
        pointTableDs.getOrCreateTable(this.pointType);

        userPoint = this.pointUsageFacade.increase(PSession.fromUsage(this.initialPointUsage))
                .block()
                .getResult();
        logger.info("userPoint = {}", userPoint);
        Assertions.assertNotNull(userPoint);

    }


    public String getPointType() {
        return this.pointType;
    }

    public String getUid() {
        return this.uid;
    }

    public String getSubUid() {
        return this.subUid;
    }

    public PointUsage getInitialPointUsage() {
        return initialPointUsage;
    }

    public PointPo getUserPoint() {
        return this.userPoint;
    }

    public String getTag() {
        return this.tag;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public Integer getPayType() {
        return payType;
    }

    public List<String> getOrderNoList() {
        return this.orderNoList;
    }

    public String getOrderNo(int idx) {
        try {
            return orderNoList.get(idx);
        } catch (Exception e) {
            logger.warn("invalidparam, idx = {}, orderNoList = {}", idx, this.orderNoList);
            return null;
        }
    }
}
