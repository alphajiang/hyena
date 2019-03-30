package com.aj.hyena.utils;

import com.aj.hyena.HyenaConstants;

public class TableNameHelper {

    public static String getPointTableName(String type) {
        return HyenaConstants.PREFIX_POINT_TABLE_NAME + type;
    }

    public static String getPointRecTableName(String type) {
        return HyenaConstants.PREFIX_POINT_TABLE_NAME + type + "_rec";
    }

    public static String getPointRecLogTableName(String type) {
        return HyenaConstants.PREFIX_POINT_TABLE_NAME + type + "_rec_log";
    }
}
