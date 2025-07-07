package com.multiply.esl_interface.v1.global.contant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MEMBER_TYPE {
	ADMIN("ADMIN", "관리자"),
	CLIENT("CLIENT", "클라이언트");

	private String code;
	private String value;

	@JsonValue
	public String getMemberType() {
		return code;
	}

	// @JsonCreator
	// public static MEMBER_TYPE getEnumForCode(String code) {
	// 	Optional<MEMBER_TYPE> memberType = Arrays.stream(MEMBER_TYPE.values()).filter(v -> v.getCode().equals(code))
	// 											 .findFirst();
	// 	return memberType.orElse(null);
	// }
}
