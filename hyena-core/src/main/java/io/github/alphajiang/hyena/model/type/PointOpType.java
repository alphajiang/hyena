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

package io.github.alphajiang.hyena.model.type;

/**
 * 变动类型
 */
public enum PointOpType {

    INCREASE(1),    // 增加

    DECREASE(2),    // 减少(使用)

    FREEZE(3),      // 冻结

    UNFREEZE(4),    // 解冻

    EXPIRE(5),      // 过期

    CANCEL(6),      // 作废

    REFUND(7),      // 退款

    REFUND_FREEZE(8),   // 退款冻结

    REFUND_UNFREEZE(9),     // 退款解冻

    UNKNOWN(0)
    ;


    private final int code;

    PointOpType(int code) {
        this.code = code;
    }

    public static PointOpType fromCode(int code){
        for (PointOpType type : values()) {
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
