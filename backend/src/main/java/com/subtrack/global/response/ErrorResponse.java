package com.subtrack.global.response;

import com.subtrack.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private boolean success;
    private String message;
    private ErrorBody error;

    private ErrorResponse(ErrorCode errorCode, String message, List<FieldErrorDetail> details) {
        this.success = false;
        this.message = message;
        this.error = new ErrorBody(errorCode.getCode(), details);
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode, errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> details) {
        return new ErrorResponse(errorCode, errorCode.getMessage(), new ArrayList<>(details));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorBody {
        private String code;
        private List<FieldErrorDetail> details;
    }

    @Getter
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String reason;
    }
}
