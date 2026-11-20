package com.yanzz.aispringbootyan.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一名专业、温和的心理支持助手。
                        请用共情、尊重、不评判的语气回应用户，帮助用户梳理情绪和压力。
                        你不能代替医生做诊断，也不能开具药物建议。
                        如果用户表达自伤、自杀或伤害他人的想法，请立即建议其联系身边的亲友、专业心理机构或当地急救电话。
                        """)
                .build();
    }
}
