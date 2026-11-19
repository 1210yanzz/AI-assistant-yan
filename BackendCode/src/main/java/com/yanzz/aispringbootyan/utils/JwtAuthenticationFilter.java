package com.yanzz.aispringbootyan.utils;

import com.yanzz.aispringbootyan.DTO.response.UserLoginResponseDTO;
import com.yanzz.aispringbootyan.Enum.UserStatus;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.config.SecurityConfig;
import com.yanzz.aispringbootyan.service.Impl.UserServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//OncePerRequestFilter表示每次请求只执行一次
public class JwtAuthenticationFilter extends OncePerRequestFilter {  // JWT认证过滤器

    @Resource
    private UserServiceImpl userServiceImpl;

    // 不需要认证的URL
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 获取请求的URL
        String requestURI = request.getRequestURI();
        // 判断请求的URL是否在不需要认证的URL列表中，如果在则返回true即不需要认证，否则返回false
        return SecurityConfig.isPublicUrl(requestURI);
    }

    // 认证逻辑
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //  获取请求的路径和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        // 打印请求的路径和方法
        System.out.println("requestURI = " + requestURI + ", method = " + method);
        // 从请求头中获取token
        String token = JwtTokenUtil.getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            // 如果token存在，验证token
            JwtTokenUtil.TokenValidationResult validationResult = JwtTokenUtil.validateToken(token);
            if (validationResult != null && validationResult.isValid()) {
                // 查询用户信息验证用户状态
                UserLoginResponseDTO.UserDetailResponseDTO userDetail = userServiceImpl.getUserDetailByUserId(validationResult.getUserId());
                if (userDetail != null && UserStatus.NORMAL.getCode().equals(userDetail.getStatus())) {
                    // 如果用户状态正常，创建Spring Security认证对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
                    );
                    // 创建UsernamePasswordAuthenticationToken对象
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            validationResult.getUsername(),//用户名
                            null,//密码
                            authorities //spring security认证对象
                    );
                    // 设置认证信息到spring security上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    // 把token存储到请求属性中
                    request.setAttribute("jwtToken", token);
                } else {
                    // 如果用户状态异常，清理上下文
                    clearSecurityContext();
                    ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                }
            } else {
                // 如果token无效，清理上下文
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }
        } else {
            // 如果token不存在，清理上下文
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        // 继续执行下一个过滤器
        filterChain.doFilter(request, response);
    }

    // 清理Spring Security上下文
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
