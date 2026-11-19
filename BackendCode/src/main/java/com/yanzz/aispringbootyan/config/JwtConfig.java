package com.yanzz.aispringbootyan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")  // 配置属性前缀，自动绑定配置属性
public class JwtConfig {
    private String secret; // jwt密钥，用于签名和验证token
    private int expiration; // token有效期：24小时（毫秒）
    private int refreshExpiration; // refresh_token有效期：7天（毫秒）
    private String tokenPrefix; // token前缀
    private String header; // header名称
    private String tokenType; // token类型
}
