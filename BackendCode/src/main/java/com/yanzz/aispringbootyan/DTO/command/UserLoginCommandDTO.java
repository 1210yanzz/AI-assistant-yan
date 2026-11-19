package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 接收前端请求：用户登录的入参对象，前端提交登录表单时使用
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class UserLoginCommandDTO {
    // 登录账号，可以是用户名也可以是邮箱，登录服务里再区分
    // @NotBlank 是 JSR-303 参数校验注解，不能为 null、空串或纯空格
    // @Size 校验长度 0-100，max=100 指定最大长度
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(max = 100, message = "用户名或邮箱长度不能超过100个字符")
    private String username;

    // 登录密码
    // @NotBlank 校验非空白
    // @Size 校验长度 6-50，min=6 最小长度，max=50 最大长度
    @NotBlank(message = "密码不能为空")
    @Size(max = 50, min = 6, message = "密码长度必须在6-50个字符之间")
    private String password;
}