package com.credit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public Result<?> handleBindException(org.springframework.validation.BindException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.error("参数绑定异常: {}", message);
        return Result.error(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(jakarta.validation.ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.error("参数校验异常: {}", message);
        return Result.error(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 方法参数异常处理
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.error("方法参数校验异常: {}", message);
        return Result.error(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后再试");
    }

}
