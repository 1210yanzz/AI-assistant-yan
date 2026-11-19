package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

import java.time.LocalDateTime;

// 返回给前端展示：咨询会话的返回对象，会话列表页使用，额外带最后一条消息、消息数、持续时长等列表友好字段
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class ConsultationSessionVO {
    // 会话ID
    // Long 是 long 的包装类型，适合数据库主键
    private Long id;

    // 用户ID
    private Long userId;

    // 用户昵称，关联 user 表查到
    private String userNickname;

    // 会话标题
    private String sessionTitle;

    // 开始时间
    // LocalDateTime 是 Java 8 日期时间类型，包含年月日时分秒
    private LocalDateTime startedAt;

    // 最后一条消息内容，列表里预览用
    private String lastMessageContent;

    // 最后一条消息时间
    // LocalDateTime 日期时间类型
    private LocalDateTime lastMessageTime;

    // 消息数量
    private Integer messageCount;

    // 持续分钟数
    private Integer durationMinutes;
}