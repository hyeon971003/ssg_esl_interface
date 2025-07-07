package com.multiply.esl_interface.v1.global.common.handler;

import com.multiply.esl_interface.v1.global.common.dto.ErrorField;
import com.multiply.esl_interface.v1.global.common.dto.ErrorResponse;
import com.multiply.esl_interface.v1.global.contant.ERROR_CODE;
import com.multiply.esl_interface.v1.global.exception.*;
import com.multiply.esl_interface.v1.global.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class APIExceptionHandler {
	private final MessageUtil messageUtil;

	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler(value = {AccessDeniedException.class})
	public ErrorResponse handleAccessDeniedException(AccessDeniedException exception) {
		String errorCode = ERROR_CODE.FORBIDDEN_ERR.getErrorCode();

		log.error("AccessDenied Exception ErrorCode :{}", errorCode);
		log.error("AccessDenied Exception ErrorMessage :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = {AuthenticationException.class})
	public ErrorResponse handleAuthenticationException(AuthenticationException exception) {
		String errorCode = ERROR_CODE.UNAUTHORIZED_ERR.getErrorCode();

		log.error("Authentication Exception ErrorCode :{}", errorCode);
		log.error("Authentication Exception ErrorMessage :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = {InvalidTokenException.class})
	public ErrorResponse handleInvalidTokenException(InvalidTokenException exception) {
		String errorCode = ERROR_CODE.INVALID_TOKEN_ERR.getErrorCode();

		log.error("Authentication Exception ErrorCode :{}", errorCode);
		log.error("Authentication Exception ErrorMessage :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, exception.getInvalidReason().name());
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = {SofiaException.class})
	public ErrorResponse handleSofiaException(SofiaException sofiaException) {
		String errorCode = sofiaException.getErrorCode().getErrorCode();
		String errorMessage = messageUtil.getMessage(errorCode);

		log.error("SofiaException ErrorCode :{}", errorCode);

		if(StringUtils.isEmpty(errorMessage)) {
			errorCode = ERROR_CODE.INTERNAL_SERVER_ERR.getErrorCode();
			errorMessage = messageUtil.getMessage(errorCode);

			log.error("SofiaException ErrorMessage Empty ");

			return ErrorResponse.of(errorCode, errorMessage);
		}
		log.error("SofiaException ErrorMessage :{}", errorMessage);

		return ErrorResponse.of(errorCode, errorMessage);
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = {Exception.class, RuntimeException.class})
	public ErrorResponse handleRuntimeException(Exception exception) {
		String errorCode = ERROR_CODE.INTERNAL_SERVER_ERR.getErrorCode();
		String errorMessage = messageUtil.getMessage(errorCode);

		log.error("Exception ErrorCode :{}", errorCode);
		log.error("Exception ErrorMessage :{}", errorMessage);
		log.error("Exception Real ErrorMessage :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, errorMessage);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
	public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
		String errorCode = ERROR_CODE.METHOD_NOT_ALLOWED_ERR.getErrorCode();

		log.error("MethodNotAllowed Exception ErrorCode : {}", errorCode);
		log.error("MethodNotAllowed Exception Real ErrorMessage: {}", exception.getMessage());

		return ErrorResponse.of(errorCode, exception.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
													  BindingResult bindingResult) {
		String errorCode = ERROR_CODE.BAD_REQUEST_ERR.getErrorCode();

		log.error("MethodArgumentNotValid Exception ErrorCode : {}", errorCode);
		log.error("MethodArgumentNotValid Exception Real ErrorMessage : {}", exception.getMessage());

		return ErrorResponse.of(errorCode,
			messageUtil.getMessage(errorCode), getErrorFields(bindingResult));
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {BadRequestException.class})
	public ErrorResponse handleBadRequestException(BadRequestException badRequestException) {
		String errorCode = ERROR_CODE.BAD_REQUEST_ERR.getErrorCode();

		log.error("Bad Request Exception ErrorCode : {}", errorCode);
		log.error("Bad Request Exception Real ErrorMessage : {}", badRequestException.getMessage());

		return ErrorResponse.of(errorCode,
			messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ErrorResponse handleHttpMessageNotReadable(Exception e) {
		String errorCode = ERROR_CODE.BAD_REQUEST_ERR.getErrorCode();

		log.error("HttpMessage Not Readable Exception ErrorCode : {}", errorCode);
		log.error("HttpMessage Not Readable Exception Real ErrorMessage : {}", e.getMessage());

		return ErrorResponse.of(errorCode,
			messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = {NoHandlerFoundException.class, NotFoundException.class})
	public ErrorResponse handleNotFound(Exception exception) {
		String errorCode = ERROR_CODE.NOT_FOUND_ERR.getErrorCode();

		log.error("Not Found ErrorCode : {}", errorCode);
		log.error("Not Found Real ErrorMessage : {}", exception.getMessage());

		return ErrorResponse.of(errorCode,
			messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler(value = {MemberExistException.class})
	public ErrorResponse handleMemberExistException(MemberExistException exception) {
		String errorCode = ERROR_CODE.MEMBER_CONFLICT_ERR.getErrorCode();

		log.error("Member Exist Exception ErrorCode :{}", errorCode);
		log.error("Member Exist Exception id :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler(value = {NotAvailableLoginIdException.class})
	public ErrorResponse handleNotAvailableLoginIdException(NotAvailableLoginIdException exception) {
		String errorCode = ERROR_CODE.LOGIN_ID_NOT_AVAILABLE.getErrorCode();

		log.error("Not Available LoginId Exception ErrorCode :{}", errorCode);
		log.error("Not Available LoginId :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode, Arrays.asList(exception.getMessage())));
	}

	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler(value = {AlreadyExistItemException.class})
	public ErrorResponse handleAlreadyExistItemException(AlreadyExistItemException exception) {
		String errorCode = ERROR_CODE.ALREADY_EXIST_ITEM.getErrorCode();

		log.error("AlreadyExistItem Exception ErrorCode :{}", errorCode);
		log.error("AlreadyExistItem :{}", exception.getMessage());

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode, Arrays.asList(exception.getMessage())));
	}

	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler(value = {ChildItemExistException.class})
	public ErrorResponse handleChildItemExistException(ChildItemExistException exception) {
		String errorCode = ERROR_CODE.CHILD_ITEM_EXIST.getErrorCode();

		log.error("ChildItemExist Exception ErrorCode :{}", errorCode);

		return ErrorResponse.of(errorCode, messageUtil.getMessage(errorCode));
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = {ResourceNotFoundException.class})
	public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException exception) {
		String errorCode = ERROR_CODE.NOT_FOUND_ERR.getErrorCode();

		log.error("Resource Not Found ErrorCode : {}", errorCode);
		log.error("Resource Not Found Real ErrorMessage : {}", exception.getMessage());

		return ErrorResponse.of(errorCode,
			messageUtil.getMessage(errorCode));
	}

	private List<ErrorField> getErrorFields(BindingResult bindingResult) {
		return bindingResult.getFieldErrors()
							.stream()
							.map(v -> new ErrorField(v.getField(), v.getDefaultMessage()))
							.collect(Collectors.toList());
	}
}
