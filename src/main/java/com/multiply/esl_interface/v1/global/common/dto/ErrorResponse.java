package com.multiply.esl_interface.v1.global.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {
	private String errorCode;
	private String errorMessage;

	private List<ErrorField> errorFields;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime now;

	ErrorResponse(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.now = LocalDateTime.now();
	}

	ErrorResponse(String errorCode, String errorMessage, List<ErrorField> errorFields) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorFields = errorFields;
		this.now = LocalDateTime.now();
	}

	public static ErrorResponse of(String errorCode, String errorMessage,
								   List<ErrorField> errorFields) {
		return new ErrorResponse(errorCode, errorMessage, errorFields);
	}

	public static ErrorResponse of(String errorCode, String errorMessage) {
		return new ErrorResponse(errorCode, errorMessage);
	}
}
