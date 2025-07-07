package com.multiply.esl_interface.v1.global.contant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ERROR_CODE {
	INTERNAL_SERVER_ERR("E5000"),
	METHOD_NOT_ALLOWED_ERR("E4050"),
	BAD_REQUEST_ERR("E4000"),
	NOT_FOUND_ERR("E4040"),
	UNAUTHORIZED_ERR("E4010"),
	INVALID_TOKEN_ERR("E4011"),
	FORBIDDEN_ERR("E4030"),
	MEMBER_REGISTER_FAIL("M0000"),
	MEMBER_CONFLICT_ERR("M9999"),
	LOGIN_ID_NOT_AVAILABLE("M9998"),
	NOT_EXIST_MENU_ITEM("M2000"),
	ALREADY_EXIST_ITEM("I0000"),
	CHILD_ITEM_EXIST("I0001");

	private final String errorCode;
}
