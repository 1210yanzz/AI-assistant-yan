package com.yanzz.aispringbootyan.common;

import com.yanzz.aispringbootyan.exception.BusinessException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody，意思是：
//   1. 这是一个全局通知类，可以拦截所有Controller抛的异常；
//   2. 方法返回值自动序列化成JSON写进响应体，不用手动写response
// 全局异常处理器，整个项目里所有Controller抛的异常都会被它接住
// 在系统里扮演的角色：后端报错时，不能把堆栈信息直接甩给前端（既难看又暴露内部实现），
// 这个类负责把各种异常"翻译"成统一的{ code, msg, data } JSON结构返回
public class GlobalExceptionHandler {

    // 捕获方法校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleException(MethodArgumentNotValidException e) {
        // 获取异常对象的错误信息
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        // 返回结果
        return Result.error(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getMessage(), message);
    }

    // 捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleException(BusinessException e) {
        // 获取异常对象的错误码和错误信息，如果有data
        if(e.getData() != null) {
            return Result.error(e.getCode(), e.getMessage(), e.getData());
        }
        // 如果没有data
        return Result.error(e.getCode(), e.getMessage(), null);
    }
}
