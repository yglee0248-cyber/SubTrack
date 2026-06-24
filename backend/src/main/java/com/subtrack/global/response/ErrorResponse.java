package com.subtrack.global.response;

import com.subtrack.global.exception.ErrorCode;
import java.util.Collections;
import java.util.List;

public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final ErrorBody error;

    private ErrorResponse(boolean success, String message, ErrorBody error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage(), Collections.emptyList());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return of(errorCode, message, Collections.emptyList());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<?> details) {
        return of(errorCode, errorCode.getMessage(), details);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<?> details) {
        return new ErrorResponse(false, message, new ErrorBody(errorCode.getCode(), details));
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ErrorBody getError() {
        return error;
    }

    public static class ErrorBody {
        private final String code;
        private final List<?> details;

        private ErrorBody(String code, List<?> details) {
            this.code = code;
            this.details = details;
        }

        public String getCode() {
            return code;
        }

        public List<?> getDetails() {
            return details;
        }
    }

    public static class FieldErrorDetail {
        private final String field;
        private final String reason;

        public FieldErrorDetail(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        public String getField() {
            return field;
        }

        public String getReason() {
            return reason;
        }
    }
}
