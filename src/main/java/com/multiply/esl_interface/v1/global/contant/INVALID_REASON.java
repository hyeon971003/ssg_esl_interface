package com.multiply.esl_interface.v1.global.contant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum INVALID_REASON {
	EXPIRED, MALFORMED, UNSUPPORTED, CLAIMS, AUTHORITY, ETC
}
