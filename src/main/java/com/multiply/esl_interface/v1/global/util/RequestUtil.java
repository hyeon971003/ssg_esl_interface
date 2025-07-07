package com.multiply.esl_interface.v1.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtil {

	public static HttpServletRequest getServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public static String getUri() {
		HttpServletRequest request = getServletRequest();

		return request.getRequestURI();
	}

	public static String getIpv4Address() {
		HttpServletRequest request = getServletRequest();
		String ip = request.getHeader("X-Forwarded-For");

		return StringUtils.isEmpty(ip) ? request.getRemoteAddr() : ip;
	}
}
