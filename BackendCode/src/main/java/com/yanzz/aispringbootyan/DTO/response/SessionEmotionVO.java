package com.yanzz.aispringbootyan.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 会话情绪分析结果，字段与前端 consultation.vue 的情绪花园保持一致
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEmotionVO {

    // 主要情绪，如 快乐/平静/焦虑/悲伤
    private String primaryEmotion;

    // 情绪强度 0-100，数值越高代表情绪越强烈
    private Integer emotionScore;

    // 是否为负面情绪
    private Boolean isNegative;

    // 一句温和的专业建议
    private String suggestion;

    // 风险等级 0:正常 1:关注 2:预警 3:危机
    private Integer riskLevel;

    // 具体可执行的改善建议
    private List<String> improvementSuggestions;

    // 风险描述，riskLevel >= 2 时使用
    private String riskDescription;
}
