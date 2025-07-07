package com.multiply.esl_interface.v1.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private Integer totalCount;
    private boolean result;
    private String resultMessage;

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(T data, boolean result) {
        this.data = data;
        this.result = result;
    }

    public ApiResponse(boolean result, String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;
    }

    public ApiResponse(T data, Integer totalCount, boolean result) {
        this.data = data;
        this.totalCount = totalCount;
        this.result = result;
    }

    public ApiResponse(T data, boolean result, String resultMessage) {
        this.data = data;
        this.result = result;
        this.resultMessage = resultMessage;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<T>(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(data);
    }

    public static <T> ApiResponse<T> success(T data, boolean result) {
        return new ApiResponse<T>(data, result);

    }

    public static <T> ApiResponse<T> success(boolean result, String resultMessage) {
        return new ApiResponse<T>(result, resultMessage);
    }

    public static <T> ApiResponse<T> success(T data, Integer totalCount, boolean result) {
        return new ApiResponse<T>(data, totalCount, result);
    }

    public static <T> ApiResponse<T> success(T data, boolean result, String resultMessage) {
        return new ApiResponse<T>(data, result, resultMessage);
    }

    public static <T> ApiResponse<T> success(T data, Integer totalCount, boolean result, String resultMessage) {
        return new ApiResponse<T>(data, totalCount, result, resultMessage);
    }
}