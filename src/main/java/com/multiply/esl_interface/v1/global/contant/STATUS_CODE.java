package com.multiply.esl_interface.v1.global.contant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum STATUS_CODE {
	SUCCESS(HttpStatus.OK, "200", "Success"),
	FAIL(HttpStatus.OK, "9999", "FAIL");

	private HttpStatus status;
	private String code;
	private String message;
}
