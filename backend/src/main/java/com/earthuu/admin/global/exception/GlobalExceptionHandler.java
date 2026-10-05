package com.earthuu.admin.global.exception;

import com.earthuu.admin.global.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        var code = exception.errorCode();
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), exception.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication() {
        var code = ErrorCode.INVALID_CREDENTIALS;
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ErrorResponse.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        boolean reasonMissing = exception.getBindingResult().getFieldErrors().stream()
                .anyMatch(error -> error.getField().equals("reason")
                        && (error.getRejectedValue() == null || error.getRejectedValue().toString().isBlank()));
        if (reasonMissing) {
            var code = exception.getBindingResult().getObjectName().equals("reportResolveRequest")
                    ? ErrorCode.REPORT_REASON_REQUIRED
                    : ErrorCode.REJECT_REASON_REQUIRED;
            return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message(), errors));
        }
        var code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        var code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        var code = ErrorCode.INVALID_REQUEST;
        var errors = List.of(new ErrorResponse.FieldError(exception.getName(), "지원하지 않는 값입니다."));
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message(), errors));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleObjectOptimisticLocking(ObjectOptimisticLockingFailureException exception) {
        var code = exception.getPersistentClass() != null
                && exception.getPersistentClass().getName().equals("com.earthuu.admin.report.entity.Report")
                ? ErrorCode.CONCURRENT_REPORT_UPDATE
                : ErrorCode.CONCURRENT_EVENT_UPDATE;
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking() {
        var code = ErrorCode.CONCURRENT_EVENT_UPDATE;
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code.name(), code.message()));
    }
}
