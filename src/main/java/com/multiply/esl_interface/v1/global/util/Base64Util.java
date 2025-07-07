package com.multiply.esl_interface.v1.global.util;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class Base64Util {
	public static String encode(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static String decode(String encodeStr) {
		return new String(Base64.getDecoder().decode(encodeStr));
	}
}
