package com.yanzz.aispringbootyan.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// 返回给前端展示：咨询消息的返回对象，额外带发送者类型描述、消息类型描述、消息长度等展示友好字段
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class ConsultationMessageResponseDTO {
    // 消息ID
    // Long 是 long 的包装类型，适合数据库主键 ID
    private Long id;

    // 所属会话ID
    private Long sessionId;

    // 发送者类型 1:用户 2:AI助手
    // Integer 是 int 的包装类型，可以为 null
    private Integer senderType;

    // 发送者类型的中文描述（"用户"/"AI助手"）
    private String senderTypeDesc;

    // 消息类型 1:文本
    private Integer messageType;

    // 消息类型的中文描述
    private String messageTypeDesc;

    // 消息正文
    private String content;

    // 情绪标签
    private String emotionTag;

    // 使用的AI模型
    private String aiModel;

    // 创建时间
    // LocalDateTime 是 Java 8 日期时间类型，包含年月日时分秒，适合记录精确时间点
    private LocalDateTime createdAt;

    // 消息长度（字符数），前端展示或做限制时用
    private Integer contentLength;

    // 根据 content 计算消息长度并存到 contentLength 字段，内容为 null 时长度记 0
    public void calculateContentLength() {
        this.contentLength = content != null ? content.length() : 0;
    }
}