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

package io.github.alphajiang.hyena.utils;

import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.junit.Assert;

public class HyenaTestAssert {

    public static void assertEquals(PointPo expect, PointPo actual) {
        Assert.assertEquals(expect.getUid(), actual.getUid());
        Assert.assertEquals(expect.getName(), actual.getName());
        Assert.assertEquals(expect.getPoint(), actual.getPoint());
        Assert.assertEquals(expect.getAvailable(), actual.getAvailable());
        Assert.assertEquals(expect.getUsed(), actual.getUsed());
        Assert.assertEquals(expect.getFrozen(), actual.getFrozen());
        Assert.assertEquals(expect.getRefund(), actual.getRefund());
        Assert.assertEquals(expect.getExpire(), actual.getExpire());
        Assert.assertEquals(expect.getCost(), actual.getCost());
        Assert.assertEquals(expect.getFrozenCost(), actual.getFrozenCost());
        Assert.assertEquals(expect.getSeqNum(), actual.getSeqNum());
        Assert.assertEquals(expect.getEnable(), actual.getEnable());

    }

    public static void assertEquals(PointLogPo expect, PointLogPo actual) {
        Assert.assertEquals(expect.getUid(), actual.getUid());
        Assert.assertEquals(expect.getType().intValue(), actual.getType().intValue());
        Assert.assertEquals(expect.getSeqNum(), actual.getSeqNum());
        Assert.assertEquals(expect.getDelta().longValue(), actual.getDelta().longValue());
        Assert.assertEquals(expect.getDeltaCost().longValue(), actual.getDeltaCost().longValue());
        Assert.assertEquals(expect.getPoint().longValue(), actual.getPoint().longValue());
        Assert.assertEquals(expect.getAvailable().longValue(), actual.getAvailable().longValue());
        Assert.assertEquals(expect.getUsed().longValue(), actual.getUsed().longValue());
        Assert.assertEquals(expect.getFrozen().longValue(), actual.getFrozen().longValue());
        Assert.assertEquals(expect.getExpire().longValue(), actual.getExpire().longValue());
        Assert.assertEquals(expect.getRefund().longValue(), actual.getRefund().longValue());
        Assert.assertEquals(expect.getCost().longValue(), actual.getCost().longValue());
        Assert.assertEquals(expect.getFrozenCost().longValue(), actual.getFrozenCost().longValue());

        if (expect.getTag() != null) {
            Assert.assertEquals(expect.getTag(), actual.getTag());
        }
        if (expect.getOrderNo() != null) {
            Assert.assertEquals(expect.getOrderNo(), actual.getOrderNo());
        }
        if (expect.getSourceType() != null) {
            Assert.assertEquals(expect.getSourceType().intValue(), actual.getSourceType().intValue());
        }
        if (expect.getOrderType() != null) {
            Assert.assertEquals(expect.getOrderType().intValue(), actual.getOrderType().intValue());
        }
        if (expect.getPayType() != null) {
            Assert.assertEquals(expect.getPayType().intValue(), actual.getPayType().intValue());
        }
        Assert.assertEquals(expect.getExtra(), actual.getExtra());
        if (expect.getNote() != null) {
            Assert.assertEquals(expect.getNote(), actual.getNote());
        }
    }

    public static void assertEquals(PointRecPo expect, PointRecPo actual) {

        Assert.assertEquals(expect.getPid(), actual.getPid());
        Assert.assertEquals(expect.getSeqNum(), actual.getSeqNum());
        Assert.assertEquals(expect.getTotal().longValue(), actual.getTotal().longValue());
        Assert.assertEquals(expect.getAvailable().longValue(), actual.getAvailable().longValue());
        Assert.assertEquals(expect.getUsed().longValue(), actual.getUsed().longValue());
        Assert.assertEquals(expect.getFrozen().longValue(), actual.getFrozen().longValue());
        Assert.assertEquals(expect.getCancelled().longValue(), actual.getCancelled().longValue());
        Assert.assertEquals(expect.getExpire().longValue(), actual.getExpire().longValue());
        Assert.assertEquals(expect.getTotalCost().longValue(), actual.getTotalCost().longValue());
        Assert.assertEquals(expect.getFrozenCost().longValue(), actual.getFrozenCost().longValue());
        Assert.assertEquals(expect.getUsedCost().longValue(), actual.getUsedCost().longValue());
        Assert.assertEquals(expect.getRefundCost().longValue(), actual.getRefundCost().longValue());

        Assert.assertEquals(expect.getTag(), actual.getTag());
        Assert.assertEquals(expect.getOrderNo(), actual.getOrderNo());
        if (expect.getSourceType() != null) {
            Assert.assertEquals(expect.getSourceType().intValue(), actual.getSourceType().intValue());
        }
        if (expect.getOrderType() != null) {
            Assert.assertEquals(expect.getOrderType().intValue(), actual.getOrderType().intValue());
        }
        if (expect.getPayType() != null) {
            Assert.assertEquals(expect.getPayType().intValue(), actual.getPayType().intValue());
        }
        Assert.assertEquals(expect.getExtra(), actual.getExtra());
        if (expect.getIssueTime() != null) {
            Assert.assertEquals(expect.getIssueTime(), actual.getIssueTime());
        }
        Assert.assertEquals(expect.getExpireTime(), actual.getExpireTime());

    }


    public static void assertEquals(PointRecLogPo expect, PointRecLogPo actual) {

        Assert.assertEquals(expect.getUid(), actual.getUid());
        Assert.assertEquals(expect.getPid(), actual.getPid());
        Assert.assertEquals(expect.getSeqNum(), actual.getSeqNum());
        Assert.assertEquals(expect.getRecId(), actual.getRecId());
        Assert.assertEquals(expect.getType(), actual.getType());
        Assert.assertEquals(expect.getDelta().longValue(), actual.getDelta().longValue());
        Assert.assertEquals(expect.getAvailable().longValue(), actual.getAvailable().longValue());
        Assert.assertEquals(expect.getUsed().longValue(), actual.getUsed().longValue());
        Assert.assertEquals(expect.getFrozen().longValue(), actual.getFrozen().longValue());
        Assert.assertEquals(expect.getCancelled().longValue(), actual.getCancelled().longValue());
        Assert.assertEquals(expect.getExpire().longValue(), actual.getExpire().longValue());
        Assert.assertEquals(expect.getCost().longValue(), actual.getCost().longValue());
        Assert.assertEquals(expect.getFrozenCost().longValue(), actual.getFrozenCost().longValue());
        Assert.assertEquals(expect.getUsedCost().longValue(), actual.getUsedCost().longValue());
        Assert.assertEquals(expect.getRefundCost().longValue(), actual.getRefundCost().longValue());

        if (expect.getSourceType() != null) {
            Assert.assertEquals(expect.getSourceType().intValue(), actual.getSourceType().intValue());
        }
        if (expect.getOrderType() != null) {
            Assert.assertEquals(expect.getOrderType().intValue(), actual.getOrderType().intValue());
        }
        if (expect.getPayType() != null) {
            Assert.assertEquals(expect.getPayType().intValue(), actual.getPayType().intValue());
        }
        if (expect.getNote() != null) {
            Assert.assertEquals(expect.getNote(), actual.getNote());
        }


    }
}
