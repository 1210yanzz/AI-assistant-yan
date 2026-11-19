package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// 接收前端请求：写/改情绪日记的入参对象，前端提交日记表单时使用
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class EmotionDiaryCommandDTO {
    // 记录日期，必填
    // LocalDate 是 Java 8 日期类型，只包含年月日，没有时分秒，适合表示生日、日记日期等
    // @NotNull 是 JSR-303 参数校验注解，不能为 null
    @NotNull(message = "记录日期不能为空")
    private LocalDate diaryDate;

    // 情绪评分（1-10），必填
    // Integer 是 int 的包装类型，可以为 null，比基本类型更灵活
    // @NotNull 校验非空
    // @Min 校验最小值为 1，value 参数指定下限
    // @Max 校验最大值为 10，value 参数指定上限
    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分最低为1分")
    @Max(value = 10, message = "情绪评分最高为10分")
    private Integer moodScore;

    // 主要情绪，选填
    private String dominantEmotion;

    // 情绪触发因素，选填
    private String emotionTriggers;

    // 日记正文，选填
    private String diaryContent;

    // 睡眠质量（1-5），选填
    // @Min 校验最小值 1，@Max 校验最大值 5
    @Min(value = 1, message = "睡眠质量最低为1分")
    @Max(value = 5, message = "睡眠质量最高为5分")
    private Integer sleepQuality;

    // 压力水平（1-5），选填
    // @Min 校验最小值 1，@Max 校验最大值 5
    @Min(value = 1, message = "压力水平最低为1分")
    @Max(value = 5, message = "压力水平最高为5分")
    private Integer stressLevel;
}