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

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.model.exception.BaseException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.exception.HyenaStatusException;
import org.slf4j.event.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class HyenaAssert {

    private HyenaAssert() {
        // default construct
    }

    /**
     * 用于判断状态. 不匹配时抛出 ParkStatusException
     *
     * @param expression 判断条件
     * @param message    错误提示文案
     */
    public static void isTrueStatus(boolean expression, String message) {
        if (!expression) {
            throw new HyenaStatusException(message);
        }
    }


    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new HyenaServiceException(message, Level.WARN);
        }
    }

    public static void isTrue(boolean expression, int code, String message) {
        if (!expression) {
            throw new HyenaServiceException(code, message, Level.WARN);
        }
    }

    public static <T extends BaseException> void isTrue(boolean expression,
                                                        Class<T> exceptionType,
                                                        String message) {
        if (!expression) {
            try {
                throw exceptionType.getDeclaredConstructor(String.class).newInstance(message);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new HyenaServiceException(message, Level.ERROR);
            }
        }
    }

    public static void equals(String expect, String actual, String message) {

        if (!StringUtils.equals(expect, actual)) {
            throw new HyenaServiceException(message, Level.WARN);
        }
    }

    /**
     * 判断字符段 expression 是否为空. 默认抛出参数错误
     *
     * @param expression 判断条件
     * @param message    错误提示文案
     */
    public static void notBlank(String expression, String message) {
        HyenaAssert.notBlank(expression, HyenaConstants.RES_CODE_SERVICE_ERROR, message);
    }

    /**
     * 判断输入的expression是否为空字串
     *
     * @param expression 判断条件
     * @param code       错误码
     * @param message    错误提示文案
     */
    public static void notBlank(String expression, int code, String message) {
        if (!StringUtils.isNotBlank(expression)) {
            throw new HyenaServiceException(code, message);
        }
    }

    public static void notBlank(String expression, int code, String message, Level logLevel) {
        if (!StringUtils.isNotBlank(expression)) {
            throw new HyenaServiceException(code, message, logLevel);
        }
    }

    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new HyenaServiceException(message);
        }
    }

    public static void isNull(Object obj, int code, String message) {
        if (obj != null) {
            throw new HyenaServiceException(code, message);
        }
    }

    /**
     * 判断对象obj是否为null. 默认抛出参数错误(status = 10000 的错误)
     *
     * @param obj     要校验的对象
     * @param message 错误提示文案
     */
    public static void notNull(Object obj, String message) {
        HyenaAssert.notNull(obj, HyenaConstants.RES_CODE_SERVICE_ERROR, message);
    }

    /**
     * 判断对象obj是否为null.
     *
     * @param obj     要校验的对象
     * @param code    错误码
     * @param message 错误提示文案
     */
    public static void notNull(Object obj, int code, String message) {
        if (obj == null) {
            throw new HyenaServiceException(code, message, Level.WARN);
        }
    }

    public static void notNull(Object obj, int code, String message, Level logLevel) {
        if (obj == null) {
            throw new HyenaServiceException(code, message, logLevel);
        }
    }

    /**
     * 判断Collection对象是否为null或空
     *
     * @param obj     要校验的对象
     * @param message 错误提示文案
     */
    public static void notEmpty(Collection<?> obj, String message) {
        if (CollectionUtils.isEmpty(obj)) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_ERROR, message);
        }
    }
}
