package com.multiply.esl_interface.v1.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyExistItemException extends RuntimeException {

	public AlreadyExistItemException(String message) {
		super(message);
	}
}
