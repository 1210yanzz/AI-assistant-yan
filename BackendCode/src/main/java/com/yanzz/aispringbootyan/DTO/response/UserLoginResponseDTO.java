package com.yanzz.aispringbootyan.DTO.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 返回给前端展示：登录成功后的返回对象，包含 JWT 令牌、角色类型、用户详细信息
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
// @Builder 是 Lombok 注解，自动生成建造者模式，方便链式构建对象
@Data
@Builder
public class UserLoginResponseDTO {
    // JWT 令牌，前端后续请求的身份凭证
    private String token;

    // 角色类型，前端根据它决定显示哪些菜单/功能
    private String roleType;

    // 用户详细信息
    private UserDetailResponseDTO userInfo;

    // 登录返回里的用户详细信息静态内部类
    // @Builder Lombok 建造者注解
    // @Data Lombok 自动生成 getter/setter
    @Builder
    @Data
    public static class UserDetailResponseDTO {
        // 用户ID
        // Long 是 long 的包装类型，适合数据库主键
        private Long id;

        // 登录账号
        private String username;

        // 邮箱
        private String email;

        // 昵称
        private String nickname;

        // 头像地址
        private String avatar;

        // 手机号
        private String phone;

        // 性别 0:未知 1:男 2:女
        // Integer 是 int 的包装类型
        private Integer gender;

        // 性别中文描述
        private String genderDisplayName;

        // 生日
        // LocalDate 是 Java 8 日期类型，只包含年月日
        private LocalDate birthday;

        // 用户类型 1:普通用户 2:管理员
        private Integer userType;

        // 用户类型中文描述
        private String userTypeDisplayName;

        // 状态 0:禁用 1:正常
        private Integer status;

        // 状态中文描述
        private String statusDisplayName;

        // 展示名（昵称优先，否则用户名）
        private String displayName;

        // 创建时间
        // LocalDateTime 是 Java 8 日期时间类型，包含年月日时分秒
        private LocalDateTime createdAt;

        // 更新时间
        // LocalDateTime 日期时间类型
        private LocalDateTime updatedAt;
    }
}