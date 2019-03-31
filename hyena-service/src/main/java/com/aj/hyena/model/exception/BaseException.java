package com.aj.hyena.model.exception;

import org.slf4j.event.Level;

public class BaseException extends RuntimeException {

    private final int code;

    private final Level logLevel;


    public BaseException(int code, String msg) {
        super(msg);
        this.code = code;
        logLevel = Level.ERROR;
    }

    public BaseException(int code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        logLevel = Level.ERROR;
    }


    public BaseException(int code, String msg, Level logLevel) {
        super(msg);
        this.code = code;
        this.logLevel = logLevel;
    }

    public BaseException(int code, String msg, Level logLevel, Throwable e) {
        super(msg, e);
        this.code = code;
        this.logLevel = logLevel;
    }

    public int getCode() {
        return code;
    }

    public Level getLogLevel() {
        return logLevel;
    }
}
