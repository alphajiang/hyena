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
