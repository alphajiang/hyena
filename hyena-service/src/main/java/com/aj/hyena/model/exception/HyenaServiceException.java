package com.aj.hyena.model.exception;

import com.aj.hyena.HyenaConstants;
import org.slf4j.event.Level;

public class HyenaServiceException extends BaseException {
    private static final int CODE = HyenaConstants.RES_CODE_SERVICE_ERROR;

    public HyenaServiceException(String msg, Level logLevel) {
        super(CODE, msg, logLevel);
    }


    public HyenaServiceException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    public HyenaServiceException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public HyenaServiceException(int code, String msg, Level logLevel) {
        super(code, msg, logLevel);

    }

    public HyenaServiceException(String msg) {
        super(CODE, msg);
    }


    public HyenaServiceException(int code, String msg) {
        super(code, msg);
    }
}
