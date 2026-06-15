package com.subtrack.global.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공했습니다.";

    private boolean success;
    private String message;
    private T data;

    private ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(DEFAULT_SUCCESS_MESSAGE, null);
    }
}
