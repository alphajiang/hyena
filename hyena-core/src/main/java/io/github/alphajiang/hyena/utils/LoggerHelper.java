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

import org.springframework.web.server.ServerWebExchange;

import java.util.Iterator;


public class LoggerHelper {


    public static String formatEnterLog(ServerWebExchange exh) {
        return ">> " + LoggerHelper.getUserLog(exh, true);
    }

    public static String formatEnterLog(ServerWebExchange exh, boolean logParams) {
        return ">> " + LoggerHelper.getUserLog(exh, logParams);
    }

    public static String formatLeaveLog(ServerWebExchange exh) {
        return "<< " + LoggerHelper.getUserLog(exh, false);
    }

    private static String getUserLog(ServerWebExchange exh, boolean logParams) {

        StringBuilder sb = new StringBuilder();


        if (logParams) {
            sb.append("params = { ");
            boolean firstKey = true;
            Iterator<String> iter = exh.getRequest().getQueryParams().keySet().iterator();
            while (iter.hasNext()) {
                String k = iter.next();

                if (firstKey) {
                    firstKey = false;
                } else {
                    sb.append(", ");
                }

                sb.append(k).append("=[");
                boolean firstValue = true;
                for (String v : exh.getRequest().getQueryParams().get(k)) {
                    if (firstValue) {
                        firstValue = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(v);
                }
                sb.append("]");
            }
            sb.append(" } ");
        }
        return sb.toString();

    }
}
