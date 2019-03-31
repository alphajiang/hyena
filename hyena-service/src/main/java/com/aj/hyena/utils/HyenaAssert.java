package com.aj.hyena.utils;

import com.aj.hyena.HyenaConstants;
import com.aj.hyena.model.exception.HyenaServiceException;
import com.aj.hyena.model.exception.HyenaStatusException;
import org.slf4j.event.Level;

import java.util.Collection;

public class HyenaAssert {

    private HyenaAssert() {
        // default construct
    }

    /**
     * 用于判断状态. 不匹配时抛出 ParkStatusException
     *
     * @param message
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

    public static void equals(String expect, String actual, String message) {

        if (!StringUtils.equals(expect, actual)) {
            throw new HyenaServiceException(message, Level.WARN);
        }
    }

    /**
     * 判断字符段 expression 是否为空. 默认抛出参数错误
     *
     * @param expression
     * @param message
     */
    public static void notBlank(String expression, String message) {
        HyenaAssert.notBlank(expression, HyenaConstants.RES_CODE_SERVICE_ERROR, message);
    }

    /**
     * 判断输入的expression是否为空字串
     *
     * @param expression
     * @param code
     * @param message
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
     * @param obj
     * @param message
     */
    public static void notNull(Object obj, String message) {
        HyenaAssert.notNull(obj, HyenaConstants.RES_CODE_SERVICE_ERROR, message);
    }

    /**
     * 判断对象obj是否为null.
     *
     * @param obj
     * @param code
     * @param message
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
     * @param obj
     * @param message
     */
    public static void notEmpty(Collection<?> obj, String message) {
        if (CollectionUtils.isEmpty(obj)) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_ERROR, message);
        }
    }
}
