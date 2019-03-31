package com.aj.hyena.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static String upperCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }

    public static boolean equals(final String cs1, final String cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        return cs1.equals(cs2);
    }

    public static boolean isBlank(final String cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final String cs) {
        return !isBlank(cs);
    }


    /**
     * 将逗号分割的字符串转换为long型数组
     *
     * @param in
     * @return
     */
    public static List<Long> parseLongIds(String in) {
        if (in == null) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (String t : in.split(",")) {
            if (isBlank(t)) {
                continue;

            }
            try {
                long v = Long.valueOf(t);
                result.add(v);
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
    }

    public static List<Integer> parseIntegerIds(String in) {
        if (in == null) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (String t : in.split(",")) {
            if (isBlank(t)) {
                continue;

            }
            try {
                int v = Integer.valueOf(t);
                result.add(v);
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
    }

    public static List<String> splitStringList(String in) {
        if (in == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String t : in.split(",")) {
            if (isBlank(t)) {
                continue;

            }
            result.add(t);

        }
        return result;
    }

}
