package com.multiply.esl_interface.v1.global.exception;

import com.multiply.esl_interface.v1.global.contant.ERROR_CODE;
import lombok.Getter;

@Getter
public class SofiaException extends RuntimeException {
	private ERROR_CODE errorCode;

	public SofiaException(ERROR_CODE errorCode) {
		this.errorCode = errorCode;
	}
}
