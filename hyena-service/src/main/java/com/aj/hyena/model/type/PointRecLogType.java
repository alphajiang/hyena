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
