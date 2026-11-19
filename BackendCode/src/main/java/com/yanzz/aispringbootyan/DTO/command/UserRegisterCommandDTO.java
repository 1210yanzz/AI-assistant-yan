package com.yanzz.aispringbootyan.DTO.command;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

// 接收前端请求：用户注册的入参对象，前端提交注册表单时使用
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class UserRegisterCommandDTO {
    // 登录账号，全局唯一
    // @NotBlank 是 JSR-303 参数校验注解，不能为 null、空串或纯空格，message 是校验失败提示
    // @Size 校验长度 3-50，min=3 最小长度，max=50 最大长度
    // @Pattern 正则校验，regexp 指定正则表达式：^[a-zA-Z0-9_]+$ 表示只允许字母、数字和下划线
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    // 邮箱，需要合法格式
    // @NotBlank 校验非空白
    // @Email 是 JSR-303 邮箱格式校验注解
    // @Size 校验最大长度 100
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    // 昵称，选填
    // @Size 校验最大长度 50
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    // 手机号，选填，校验国内 11 位格式
    // @Pattern 正则校验：^1[3-9]\d{9}$ 表示 1 开头、第二位 3-9、后面跟 9 位数字，共 11 位
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    // 密码
    // @NotBlank 校验非空白
    // @Size 校验长度 6-50
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    private String password;

    // 确认密码，注册逻辑里要和 password 比对是否一致
    // @NotBlank 校验非空白
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 性别 0:未知 1:男 2:女
    private Integer gender;

    // 用户类型，默认 1 即普通用户；只有特殊场景才指定管理员
    private Integer userType = 1;

    // 生日
    // LocalDate 是 Java 8 日期类型，只包含年月日
    private LocalDate birthday;
}