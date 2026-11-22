package com.yanzz.aispringbootyan.AiService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.DTO.response.SessionEmotionVO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// 会话情绪分析：把最近对话交给 DeepSeek，解析成前端情绪花园需要的结构并缓存到会话表
@Service
public class EmotionAnalysisService {

    // 只取最近 20 条消息，控制上下文长度和调用成本
    private static final int HISTORY_LIMIT = 20;

    private static final String ANALYSIS_SYSTEM_PROMPT = """
            你是一名专业的心理情绪分析助手。请根据最近的心理咨询对话，分析用户当前的情绪状态。
            只输出一个 JSON 对象，不要输出 Markdown 代码块、解释或其他文字。JSON 字段如下：
            {
              "primaryEmotion": "主要情绪，简短，例如 快乐/平静/焦虑/悲伤/愤怒/恐惧/压力",
              "emotionScore": 0 到 100 的整数，数值越高代表情绪越强烈,
              "isNegative": true 或 false,
              "suggestion": "一句温和、专业的建议",
              "riskLevel": 0 到 3 的整数，0 正常、1 关注、2 预警、3 危机,
              "improvementSuggestions": ["3 条具体可执行的改善建议"],
              "riskDescription": "riskLevel >= 2 时描述风险，否则为空字符串"
            }
            如果对话内容不足以判断，请返回中性、平稳的结果，不要编造风险。
            """;

    @Resource
    @Qualifier("emotion-analysis")
    private ChatClient emotionAnalysisChatClient;

    @Resource
    private ConsultationSessionService consultationSessionService;

    @Resource
    private ConsultationMessageService consultationMessageService;

    public SessionEmotionVO analyze(ConsultationSession session) {
        Long sessionId = session.getId();
        ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(sessionId);
        SessionEmotionVO cached = parseCached(session.getLastEmotionAnalysis());

        // 分析时间晚于最后一条消息时直接返回缓存，避免重复调用大模型
        if (cached != null && isCacheFresh(session, lastMessage)) {
            return cached;
        }

        List<ConsultationMessage> messages = consultationMessageService.listRecentMessages(sessionId, HISTORY_LIMIT);
        if (messages.isEmpty()) {
            return cached != null ? cached : defaultEmotion();
        }

        try {
            String conversation = messages.stream()
                    .map(this::formatMessage)
                    .collect(Collectors.joining("\n"));

            String content = emotionAnalysisChatClient.prompt()
                    .system(ANALYSIS_SYSTEM_PROMPT)
                    .user("以下是最近的心理咨询对话：\n" + conversation)
                    .call()
                    .content();

            SessionEmotionVO analysis = parseAnalysis(content);
            consultationSessionService.saveEmotionAnalysis(sessionId, JSONUtil.toJsonStr(analysis));
            return analysis;
        } catch (Exception e) {
            // 分析失败不能影响聊天主流程，优先返回旧缓存，否则返回中性结果
            System.err.println("AI 情绪分析失败: " + e.getMessage());
            return cached != null ? cached : defaultEmotion();
        }
    }

    private boolean isCacheFresh(ConsultationSession session, ConsultationMessageResponseDTO lastMessage) {
        if (session.getLastEmotionUpdatedAt() == null) {
            return false;
        }
        return lastMessage == null || lastMessage.getCreatedAt() == null
                || !session.getLastEmotionUpdatedAt().isBefore(lastMessage.getCreatedAt());
    }

    private SessionEmotionVO parseCached(String analysisJson) {
        if (StrUtil.isBlank(analysisJson)) {
            return null;
        }
        try {
            SessionEmotionVO analysis = JSONUtil.toBean(analysisJson, SessionEmotionVO.class);
            normalize(analysis);
            return analysis;
        } catch (Exception e) {
            return null;
        }
    }

    private SessionEmotionVO parseAnalysis(String content) {
        if (StrUtil.isBlank(content)) {
            return defaultEmotion();
        }
        String json = extractJson(content);
        SessionEmotionVO analysis = JSONUtil.toBean(json, SessionEmotionVO.class);
        normalize(analysis);
        return analysis;
    }

    // 模型偶尔会带 ```json 代码块或前后说明文字，这里截取第一个 { 到最后一个 }
    private String extractJson(String content) {
        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private void normalize(SessionEmotionVO analysis) {
        if (analysis == null) {
            return;
        }
        if (StrUtil.isBlank(analysis.getPrimaryEmotion())) {
            analysis.setPrimaryEmotion("中性");
        }
        if (analysis.getEmotionScore() == null) {
            analysis.setEmotionScore(50);
        } else {
            analysis.setEmotionScore(Math.max(0, Math.min(100, analysis.getEmotionScore())));
        }
        analysis.setIsNegative(Boolean.TRUE.equals(analysis.getIsNegative()));
        if (StrUtil.isBlank(analysis.getSuggestion())) {
            analysis.setSuggestion("情绪状态平稳");
        }
        if (analysis.getRiskLevel() == null) {
            analysis.setRiskLevel(0);
        } else {
            analysis.setRiskLevel(Math.max(0, Math.min(3, analysis.getRiskLevel())));
        }
        if (analysis.getImprovementSuggestions() == null) {
            analysis.setImprovementSuggestions(List.of());
        }
        if (analysis.getRiskDescription() == null) {
            analysis.setRiskDescription("");
        }
    }

    private String formatMessage(ConsultationMessage message) {
        String role = message.getSenderType() != null && message.getSenderType() == 1 ? "用户" : "AI助手";
        return role + "：" + StrUtil.blankToDefault(message.getContent(), "");
    }

    private SessionEmotionVO defaultEmotion() {
        return new SessionEmotionVO("中性", 50, false, "情绪状态平稳", 0, List.of(), "");
    }
}
