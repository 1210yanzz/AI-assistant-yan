package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 返回给前端展示：情绪日记的返回对象，多带用户名、昵称等展示友好字段，前端无需额外查用户信息
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class EmotionDiaryVO {
    // 日记ID
    // Long 是 long 的包装类型，适合数据库主键
    private Long id;

    // 用户ID
    private Long userId;

    // 用户昵称，关联 user 表查到
    private String nickname;

    // 登录账号，关联 user 表查到
    private String username;

    // 记录日期
    // LocalDate 是 Java 8 日期类型，只包含年月日
    private LocalDate diaryDate;

    // 情绪评分（1-10）
    // Integer 是 int 的包装类型
    private Integer moodScore;

    // 主要情绪
    private String dominantEmotion;

    // 睡眠质量（1-5）
    private Integer sleepQuality;

    // 压力水平（1-5）
    private Integer stressLevel;

    // 情绪触发因素
    private String emotionTriggers;

    // 日记正文
    private String diaryContent;

    // AI情绪分析结果（JSON格式）
    private String aiEmotionAnalysis;

    // 创建时间
    // LocalDateTime 是 Java 8 日期时间类型，包含年月日时分秒
    private LocalDateTime createdAt;

    // 更新时间
    // LocalDateTime 日期时间类型
    private LocalDateTime updatedAt;
}