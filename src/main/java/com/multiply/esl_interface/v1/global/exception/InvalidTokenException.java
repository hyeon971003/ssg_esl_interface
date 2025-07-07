package com.multiply.esl_interface.v1.global.exception;

import com.multiply.esl_interface.v1.global.contant.INVALID_REASON;
import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {
	private INVALID_REASON invalidReason;

	public InvalidTokenException(INVALID_REASON invalidReason) {
		this.invalidReason = invalidReason;
	}
}
