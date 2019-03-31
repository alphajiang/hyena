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
