package com.example.gaokao.common;

import com.example.gaokao.common.exception.BusinessException;
import com.example.gaokao.common.exception.DifyApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(DifyApiException.class)
    public Result<Void> handleDifyApiException(DifyApiException e) {
        log.error("Dify API call failed", e);
        return Result.fail(toHttpLikeCode(e.getErrorCode()), e.getMessage());
    }

    @ExceptionHandler({DataAccessException.class})
    public Result<Void> handleDataAccessException(Exception e) {
        log.error("Database error", e);
        return Result.fail("系统繁忙，请稍后重试。");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e) {
        return Result.fail("请求参数不正确，请检查后重试。");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.fail("系统繁忙，请稍后重试。");
    }

    private int toHttpLikeCode(String errorCode) {
        if ("DIFY_AUTH_ERROR".equals(errorCode)) {
            return 401;
        }
        if ("DIFY_CONFIG_ERROR".equals(errorCode)) {
            return 422;
        }
        if ("DIFY_TIMEOUT".equals(errorCode)) {
            return 504;
        }
        if ("DIFY_RATE_LIMIT".equals(errorCode)) {
            return 429;
        }
        return 503;
    }
}
