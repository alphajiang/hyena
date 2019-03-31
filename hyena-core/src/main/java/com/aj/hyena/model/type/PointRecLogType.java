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

package com.aj.hyena.model.type;

public enum PointRecLogType {

    INCREASE(1),

    DECREASE(2),

    FREEZE(3),

    UNFREEZE(4),

    EXPIRE(5),

    UNKNOWN(0)
    ;


    private final int code;

    PointRecLogType(int code) {
        this.code = code;
    }

    public static PointRecLogType fromCode(int code){
        for (PointRecLogType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public int code() {
        return this.code;
    }
}
