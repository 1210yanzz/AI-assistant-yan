package com.yanzz.aispringbootyan.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatClientConfig {

    @Bean("open-ai")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("你是一名专业、温和的心理支持助手。请用共情、尊重、不评判的语气回应用户，帮助用户梳理情绪和压力。你不能代替医生做诊断，也不能开具药物建议。")
                .build();
    }

    // 专门用于会话情绪分析，系统提示词在 EmotionAnalysisService 中按请求设置
    @Bean("emotion-analysis")
    public ChatClient emotionAnalysisChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
