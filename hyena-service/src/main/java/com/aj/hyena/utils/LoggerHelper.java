package com.aj.hyena.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;


public class LoggerHelper {


	public static String formatEnterLog(HttpServletRequest request) {
		return ">> " + LoggerHelper.getUserLog(request, true);
	}

	public static String formatEnterLog(HttpServletRequest request, boolean logParams) {
		return ">> " + LoggerHelper.getUserLog(request, logParams);
	}

	public static String formatLeaveLog(HttpServletRequest request) {
		return "<< " + LoggerHelper.getUserLog(request, false);
	}

	private static String getUserLog(HttpServletRequest request, boolean logParams) {

		StringBuilder sb = new StringBuilder();


		if (logParams) {
			sb.append("params = { ");
			boolean firstKey = true;
			Iterator<String> iter = request.getParameterMap().keySet().iterator();
			while (iter.hasNext()) {
				String k = iter.next();

				if (firstKey) {
					firstKey = false;
				} else {
					sb.append(", ");
				}

				sb.append(k).append("=[");
				boolean firstValue = true;
				for (String v : request.getParameterMap().get(k)) {
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
			// sb.append(MessageFormat.format("params = {0}",
			// req.getParameterMap()));

		}
		return sb.toString();

	}
}
