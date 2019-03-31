package com.aj.hyena.model.exception;

import com.aj.hyena.HyenaConstants;
import org.slf4j.event.Level;

public class HyenaNoPointException extends BaseException {
    private static final int CODE = HyenaConstants.RES_CODE_NO_ENOUGH_POINT;

    public HyenaNoPointException(String msg, Level logLevel) {
        super(CODE, msg, logLevel);
    }


    public HyenaNoPointException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    public HyenaNoPointException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public HyenaNoPointException(int code, String msg, Level logLevel) {
        super(code, msg, logLevel);

    }

    public HyenaNoPointException(String msg) {
        super(CODE, msg);
    }


    public HyenaNoPointException(int code, String msg) {
        super(code, msg);
    }
}
