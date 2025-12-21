package com.rcd.movierecommender.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.VALIDATION_ERROR,
                "请求参数校验失败", request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> String.format("%s %s", violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.toList());
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.VALIDATION_ERROR,
                "请求参数校验失败", request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.MISSING_PARAMETER,
                ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                                     HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.BAD_REQUEST,
                "无法读取请求体，请检查 JSON 格式", request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED,
                ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.BAD_REQUEST,
                ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ex.getErrorCode(),
                ex.getMessage(), request.getRequestURI(), ex.getDetails().isEmpty() ? buildRootCause(ex) : ex.getDetails());
        return ResponseEntity.status(resolveHttpStatus(ex.getErrorCode())).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        log.error("Database access exception", ex);
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.DATABASE_ERROR,
                "数据库访问异常，无法读取或写入推荐/电影数据，请稍后重试", request.getRequestURI(), buildRootCause(ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(org.apache.mahout.cf.taste.common.TasteException.class)
    public ResponseEntity<ApiErrorResponse> handleTasteException(org.apache.mahout.cf.taste.common.TasteException ex,
                                                                 HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.RECOMMENDATION_ENGINE_ERROR,
                "推荐算法执行失败：" + ex.getMessage(), request.getRequestURI(), buildRootCause(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getMessage(), request.getRequestURI(), buildRootCause(ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private List<String> buildRootCause(Throwable ex) {
        List<String> details = new ArrayList<>();
        Throwable cause = ex.getCause();
        while (cause != null) {
            details.add(cause.getMessage());
            cause = cause.getCause();
        }
        return details.isEmpty() ? null : details;
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        List<ErrorCode> badRequestCodes = Arrays.asList(ErrorCode.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ErrorCode.MISSING_PARAMETER);
        if (badRequestCodes.contains(errorCode)) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ErrorCode.METHOD_NOT_ALLOWED == errorCode) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
