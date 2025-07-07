package com.multiply.esl_interface.v1.global.contant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HEADER_KEY {
	Authorization, Reissue;

	@JsonValue
	public String getHeaderKey() {
		return this.name();
	}
}
