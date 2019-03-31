package com.aj.hyena.model.exception;

import com.aj.hyena.HyenaConstants;
import org.slf4j.event.Level;

public class HyenaParameterException extends BaseException {
    private static final int CODE = HyenaConstants.RES_CODE_PARAMETER_ERROR;

    public HyenaParameterException(String msg, Level logLevel) {
        super(CODE, msg, logLevel);
    }


    public HyenaParameterException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    public HyenaParameterException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public HyenaParameterException(int code, String msg, Level logLevel) {
        super(code, msg, logLevel);

    }

    public HyenaParameterException(String msg) {
        super(CODE, msg);
    }


    public HyenaParameterException(int code, String msg) {
        super(code, msg);
    }
}
