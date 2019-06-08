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

package io.github.alphajiang.hyena.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static String toYyyyMmDdHhMmSs(Calendar cal) {
        return DateUtils.toStringFormat(cal, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toStringFormat(Calendar cal, String pattern) {
        if (cal == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(cal.getTime());
    }


    public static Calendar fromYyyyMmDdHhMmSs(String str) throws ParseException {
        return DateUtils.fromStringFormat(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static Calendar fromStringFormat(String str, String pattern) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(str));
        return cal;

    }
}
