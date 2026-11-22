package com.yanzz.aispringbootyan.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.exception.BusinessException;

// 从当前请求的 JWT 中读取登录用户信息，供 Controller 复用
public final class CurrentUserUtil {

    // 与 user.user_type 保持一致：2 为管理员
    private static final int ADMIN_USER_TYPE = 2;

    private CurrentUserUtil() {
    }

    public static DecodedJWT getJwt() {
        String token = JwtTokenUtil.getCurrentToken();
        return JwtTokenUtil.verifyToken(token);
    }

    public static Long getUserId() {
        return getJwt().getClaim("userId").asLong();
    }

    public static boolean isAdmin() {
        Integer roleType = getJwt().getClaim("roleType").asInt();
        return roleType != null && roleType == ADMIN_USER_TYPE;
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(ResultCode.AUTHORIZED_ERROR.getCode(), "仅管理员可执行该操作");
        }
    }
}
