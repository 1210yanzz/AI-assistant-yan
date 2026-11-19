package com.yanzz.aispringbootyan.config;

import cn.hutool.core.text.AntPathMatcher;
import com.yanzz.aispringbootyan.utils.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration   // 配置类
@EnableWebSecurity   // 启用Web安全配置
@EnableMethodSecurity   // 启用方法安全配置
public class SecurityConfig {

    // 路径匹配器
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final String[] PUBLIC_URLS = {
            "/",
            "/api/user/login",
            "/api/user/add"
    };

    public static Boolean isPublicUrl(String requestUrl) {
        // 遍历公开的API路径
        for (String publicUrl : PUBLIC_URLS) {
            // 使用AntPathMatcher进行匹配，将requestUrl与publicUrl进行匹配
            if (antPathMatcher.match(publicUrl, requestUrl)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // 禁用CSRF保护
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 禁用会话管理（JWT需要）
                .authorizeHttpRequests(authorize -> authorize   // 授权请求
                        .requestMatchers(PUBLIC_URLS).permitAll()  // 公开的API，不需要认证即可访问
                        .anyRequest().authenticated()  // 其他请求需要认证
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
