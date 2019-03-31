package com.aj.hyena.model.exception;

import com.aj.hyena.HyenaConstants;
import org.slf4j.event.Level;

public class HyenaStatusException extends BaseException {
    private static final int CODE = HyenaConstants.RES_CODE_STATUS_ERROR;

    public HyenaStatusException(String msg, Level logLevel) {
        super(CODE, msg, logLevel);
    }


    public HyenaStatusException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    public HyenaStatusException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public HyenaStatusException(int code, String msg, Level logLevel) {
        super(code, msg, logLevel);

    }

    public HyenaStatusException(String msg) {
        super(CODE, msg);
    }


    public HyenaStatusException(int code, String msg) {
        super(code, msg);
    }
}
