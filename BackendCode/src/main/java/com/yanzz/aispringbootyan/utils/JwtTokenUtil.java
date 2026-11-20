package com.yanzz.aispringbootyan.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.yanzz.aispringbootyan.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
// JwtTokenUtil类实现ApplicationContextAware接口
public class JwtTokenUtil implements ApplicationContextAware {

    private static final String ISSUER = "yanzz";

    // 创建一个applicationContext静态变量，用来保存applicationContext
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        JwtTokenUtil.applicationContext = applicationContext; // 将applicationContext赋值给静态变量applicationContext
    }

    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    // 创建一个生成token的方法
    public static String generateToken(Long userId, String username, Integer roleType) {
        try {
            // 获取jwt的配置
            JwtConfig jwtConfig = getJwtConfig();
            // 生成签名算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            // 生成过期时间，当前系统时间加上过期时间
            Date expirationDate = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
            // 创建token
            return JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expirationDate)
                    .withIssuedAt(new Date()) // 添加签发时间
                    .withIssuer(ISSUER)  // 添加签发人
                    .sign(algorithm);  // 签名
        } catch (Exception e) {
            throw new RuntimeException("token生成失败：", e);
        }
    }

    // 从请求头获取token，默认读取 Authorization: Bearer <token>
    public static String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 请求头名称和前缀从 jwt 配置读取，未配置时使用标准值
        JwtConfig jwtConfig = getJwtConfig();
        String headerName = StringUtils.hasText(jwtConfig.getHeader()) ? jwtConfig.getHeader() : "Authorization";
        String tokenHeader = request.getHeader(headerName);
        // 兼容旧客户端仍在使用的 token 请求头
        if (!StringUtils.hasText(tokenHeader) && !"token".equalsIgnoreCase(headerName)) {
            tokenHeader = request.getHeader("token");
        }
        if (!StringUtils.hasText(tokenHeader)) {
            return null;
        }
        tokenHeader = tokenHeader.trim();
        // 去掉 token 前缀（如 Bearer），前缀匹配不区分大小写
        String tokenPrefix = jwtConfig.getTokenPrefix();
        if (StringUtils.hasText(tokenPrefix)) {
            String prefix = tokenPrefix.trim();
            if (!prefix.endsWith(" ")) {
                prefix = prefix + " ";
            }
            if (tokenHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
                tokenHeader = tokenHeader.substring(prefix.length()).trim();
            }
        }
        return StringUtils.hasText(tokenHeader) ? tokenHeader : null;
    }

    // 获取当前token
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                return token;
            }
            // 备用方案：从请求头直接获取（即前端给服务器的token）
            String headerToken = getTokenFromRequest(request);
            return headerToken;
        }
        return null;
    }

    // 创建一个验证token的方法
    public static TokenValidationResult validateToken(String token) {
        // 验证token
        DecodedJWT decodedJWT = verifyToken(token);
        // 解码token
        Long userId = decodedJWT.getClaim("userId").asLong();
        String username = decodedJWT.getClaim("username").asString();
        // 获取角色类型，不确定角色类型
        Integer roleType = null;  // 先初始化为null
        try {
            // 获取角色类型
            roleType = decodedJWT.getClaim("roleType").asInt();
        } catch (Exception e) {
            // 角色类型获取失败，说明不是Integer，尝试获取String
            String roleTypeStr = decodedJWT.getClaim("roleType").asString();
            if(StringUtils.hasText(roleTypeStr)) {
                // 将String转换为Integer
                roleType = Integer.parseInt(roleTypeStr);
            }
        }
        // 验证token是否有效
        if(userId != null && StringUtils.hasText(username) && roleType != null) {
            return new TokenValidationResult(userId, username, roleType, true);
        }
        return null;
    }

    // 验证token是否有效
    public static DecodedJWT verifyToken(String token) {
        if(!StringUtils.hasText(token)){
            // token为空
            throw new JWTVerificationException("Token不能为空");
        }
        // token解码
        JwtConfig jwtConfig = getJwtConfig();  // 获取jwt的配置
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());  // 生成签名算法
        // 创建JWTVerifier，用于验证token
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    // 创建一个token验证结果封装类
    public static class TokenValidationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenValidationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Integer getRoleType() {
            return roleType;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
