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

package com.aj.hyena.aop;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.biz.idempotent.HyenaIdempotent;
import com.aj.hyena.model.base.BaseResponse;
import com.aj.hyena.utils.JsonUtils;
import com.aj.hyena.utils.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
@Aspect
public class IdempotentAround {
    private final Logger logger = LoggerFactory.getLogger(IdempotentAround.class);

    @Autowired
    private HyenaIdempotent hyenaIdempotent;


    @Around("@annotation(com.aj.hyena.aop.Idempotent)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        Object[] args = point.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        BaseResponse res = null;

        String seq = request.getParameter(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY);

        res = this.preProceed(seq, method);

        if (res != null) {
            return res;
        }

        res = (BaseResponse) point.proceed(point.getArgs());

        this.postProceed(seq, res);
        return res;
    }

    private BaseResponse preProceed(String seq, Method method) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        BaseResponse res = null;
        if (StringUtils.isBlank(seq)) {
            return res;
        }
        String resMsg = this.hyenaIdempotent.getByKey(seq);
        if (StringUtils.isNotBlank(resMsg)) {    // cache match
            res = (BaseResponse) JsonUtils.fromJson(resMsg, method.getReturnType());
            logger.info("idempotent cache matched. res = {}", JsonUtils.toJsonString(res));
            return res;
        }

        if (!this.hyenaIdempotent.lock(seq)) {
            res = (BaseResponse) method.getReturnType().getDeclaredConstructor().newInstance();
            res.setStatus(HyenaConstants.RES_CODE_DUPLICATE_IDEMPOTENT);
            res.setError("请勿重复提交");

        }
        return res;
    }

    private void postProceed(String seq, BaseResponse res) {
        if (StringUtils.isNotBlank(seq)) {
            this.hyenaIdempotent.setByKey(seq, res);

            this.hyenaIdempotent.unlock(seq);
        }
    }
}
