package com.subtrack.global.exception;

import com.subtrack.global.response.ErrorResponse;
import com.subtrack.global.response.ErrorResponse.FieldErrorDetail;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, details));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Database constraint violation occurred.", ex);

        return ResponseEntity
                .status(ErrorCode.DUPLICATE_RESOURCE.getStatus())
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_RESOURCE));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(ErrorResponse.of(ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected server error occurred.", ex);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
