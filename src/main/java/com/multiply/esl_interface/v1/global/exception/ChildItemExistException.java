package com.multiply.esl_interface.v1.global.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@NoArgsConstructor
public class ChildItemExistException extends RuntimeException {
}
