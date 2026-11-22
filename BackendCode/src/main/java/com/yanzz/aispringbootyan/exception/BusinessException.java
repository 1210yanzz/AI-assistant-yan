package com.yanzz.aispringbootyan.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.code = "-1";
        this.message = message;
        this.data = null;
    }

    // 需要返回具体错误码时使用（如无权限 A0300、业务错误 6000），避免统一用 -1 被前端当成登录过期
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = null;
    }

}
