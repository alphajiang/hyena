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

package io.github.alphajiang.hyena.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.idempotent.HyenaIdempotent;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.param.PointOpParam;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Aspect
public class IdempotentAround {

    private final Logger logger = LoggerFactory.getLogger(IdempotentAround.class);

    @Autowired
    private HyenaIdempotent hyenaIdempotent;


    @Around("@annotation(Idempotent)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        Idempotent shelter = method.getAnnotation(Idempotent.class);
        String name = shelter.name();
        Class resClass = shelter.resClass();

        Object[] args = point.getArgs();

        ServerWebExchange exh = (ServerWebExchange) args[0];
        PointOpParam param = (PointOpParam) args[1];
        Mono<BaseResponse> res;

        // String seq = request.getParameter(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY);
//        String seq = param.getSeq();
        String seq = this.getSeq(exh);
        if (seq != null) {
            param.setSeq(seq);
            exh.getResponse().getHeaders().add(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY, seq);
        }
//        request.getResponse().getHeaders().set(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY, seq);

        BaseResponse preRes = this.preProceed(name, param, resClass);

        if (preRes != null) {
            return Mono.just(preRes);
        }

        res = (Mono) point.proceed(point.getArgs());
        res = res.doOnNext(o -> {
            BaseResponse restRes = (BaseResponse) o;
            if (restRes.getStatus() == 0) {
                this.postProceed(name, param, restRes);
            }
        })
        ;
        return res;
    }

    private String getSeq(ServerWebExchange exh) {
        try {
            return exh.getRequest().getHeaders().get(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY).get(0);
        } catch (Exception e) {
            logger.warn("no hyena-seq header");
        }
        return null;
    }

    private BaseResponse preProceed(String name, PointOpParam param, Class resClass)
        throws NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, InstantiationException {

        BaseResponse res = null;
        if (StringUtils.isBlank(param.getSeq())) {
            return res;
        }
        String key = getKey(name, param);

        String resMsg = this.hyenaIdempotent.getByKey(name, key);
        if (StringUtils.isNotBlank(resMsg)) {    // cache match
//            ParameterizedType monoType = (ParameterizedType) method.getGenericReturnType();
//            Type[] types = monoType.getActualTypeArguments();
//            try {
//                Type retType = ((ParameterizedType)types[0]).getActualTypeArguments()[0];
//                Class<?> retTYpe = Class.forName(types[0].getTypeName());
            if (resClass.getName().equals(BaseResponse.class.getName())) {
                res = JsonUtils.fromJson(resMsg, BaseResponse.class);
            } else if (resClass.getName().equals(ObjectResponse.class.getName())) {

                res = JsonUtils.fromJson(resMsg,
                    new TypeReference<ObjectResponse<JsonNode>>() {
                    });
            } else if (resClass.getName().equals(ListResponse.class.getName())) {

                res = JsonUtils.fromJson(resMsg,
                    new TypeReference<ListResponse<JsonNode>>() {
                    });
            } else {
                logger.error("not supported type. {}", resClass.getName());
            }

            logger.info("idempotent cache matched. res = {}", JsonUtils.toJsonString(res));
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//            }
            return res;
        }

        if (!this.hyenaIdempotent.lock(key)) {
//            res = (BaseResponse) method.getReturnType().getDeclaredConstructor().newInstance();
            res = new BaseResponse();
            res.setStatus(HyenaConstants.RES_CODE_DUPLICATE_IDEMPOTENT);
            res.setError("请勿重复提交");

        }
        return res;
    }

    private void postProceed(String name, PointOpParam param, BaseResponse res) {
        if (StringUtils.isNotBlank(param.getSeq())) {
            String key = getKey(name, param);
            res.setSeq(param.getSeq());
            this.hyenaIdempotent.setByKey(name, key, res);

            this.hyenaIdempotent.unlock(key);
        }
    }

    private String getKey(String name, PointOpParam param) {
        StringBuilder buf = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            buf.append(name).append("-");
        }
        if (StringUtils.isNotBlank(param.getType())) {
            buf.append(param.getType()).append("-");
        }
        buf.append(param.getSeq());
        return buf.toString();
    }

}
