package com.aj.hyena.utils;

import java.util.Collection;

public class CollectionUtils {
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return !CollectionUtils.isEmpty(coll);
    }

    public static String join(final Collection<?> coll, String separate) {
        if(isEmpty(coll)) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        coll.stream().forEach(o -> {
            buf.append(separate);
            if(o instanceof Character) {
                buf.append((Character)o);
            }else {
                buf.append(o.toString());
            }
        });
        if(buf.length() < separate.length()) {
            return "";
        }
        else {
            return buf.substring(separate.length());
        }
    }

}
