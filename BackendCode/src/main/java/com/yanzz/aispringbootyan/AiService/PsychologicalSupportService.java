package com.yanzz.aispringbootyan.AiService;

import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.swing.*;

@Service
public class PsychologicalSupportService {

    @Resource
    private ConsultationSessionService consultationSessionService;

    @Resource
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startStreamChatSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        // 调用数据库业务服务层，创建数据库session表会话记录
        ConsultationSession consultationSession = consultationSessionService.createConsultationSession(userId, consultationSessionCreateDTO);

        // 调用数据库业务服务层，将初始消息添加到message表，请求参数为会话ID和初始消息内容
        ConsultationMessage consultationMessage = consultationMessageService.saveMessage(consultationSession.getId(), consultationSessionCreateDTO.getInitialMessage(), null);

        // 创建会话消息
        String sessionId = "session_" + consultationSession.getId();  // 会话ID，格式为session_会话ID
        // new一个会话消息对象
        StructOutPut.StreamChatSession session = new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                consultationSessionCreateDTO.getInitialMessage(),
                System.currentTimeMillis(),  // 会话创建时间，为系统当前时间
                System.currentTimeMillis() + 86400000L,  // 会话有效期1天，24小时
                1,  // 会话数，初始为1
                "ACTIVE");// 会话状态，初始为ACTIVE
        return session;
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String message) {
        // 创建Flux流式响应
        return Flux.create(sink -> {
            // sink.next("数据"); // 发送数据
            // sink.complete(); // 完成流
            // sink.error(new RuntimeException("Error")); // 发生错误
            Long sessionIdLong = getSessionId(sessionId);
            if (sessionIdLong == null) {
                sink.error(new RuntimeException("无效的会话ID"));
                return;
            }
            // 判断是否为初始消息，避免重复保存
            boolean isInitialMessage = false;  // 初始消息标志
            Integer messageCount = consultationMessageService.getMessageCountBySessionId(sessionIdLong);
            if (messageCount == 1) {
                // 查询最后一条消息
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(sessionIdLong);
                if(lastMessage != null && lastMessage.getSenderType() == 1 && message.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            }
            if (!isInitialMessage) {
                // 保存消息到数据库
                consultationMessageService.saveMessage(sessionIdLong, message, null);
            }
        });
    }

    // 获取sessionId
    public Long getSessionId(String sessionId) {
        if(sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }

}
