package com.yanzz.aispringbootyan.AiService;

import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PsychologicalSupportService {

    // 带入 AI 上下文的历史消息条数
    private static final int HISTORY_LIMIT = 20;
    private static final String AI_MODEL = "deepseek-chat";
    private static final int SENDER_USER = 1;
    private static final int SENDER_AI = 2;

    @Resource
    private ConsultationSessionService consultationSessionService;

    @Resource
    private ConsultationMessageService consultationMessageService;

    private final ChatClient chatClient;

    public PsychologicalSupportService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

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

    // 流式调用 DeepSeek，返回增量文本
    public Flux<String> streamPsychologicalChat(Long userId, String sessionId, String message) {
        Long sessionIdLong = getSessionId(sessionId);
        if (sessionIdLong == null) {
            return Flux.error(new BusinessException("无效的会话ID"));
        }

        // 判断是否为初始消息，避免重复保存
        boolean isInitialMessage = false;
        Integer messageCount = consultationMessageService.getMessageCountBySessionId(sessionIdLong);
        if (messageCount != null && messageCount == 1) {
            ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(sessionIdLong);
            if (lastMessage != null && lastMessage.getSenderType() != null
                    && lastMessage.getSenderType() == SENDER_USER
                    && message.equals(lastMessage.getContent())) {
                isInitialMessage = true;
            }
        }

        // 先取历史消息再保存当前消息，避免当前消息在上下文里出现两次
        List<Message> history = consultationMessageService.listRecentMessages(sessionIdLong, HISTORY_LIMIT)
                .stream()
                .filter(item -> item.getContent() != null)
                .map(item -> item.getSenderType() != null && item.getSenderType() == SENDER_AI
                        ? (Message) new AssistantMessage(item.getContent())
                        : (Message) new UserMessage(item.getContent()))
                .collect(Collectors.toList());

        // 初始消息在创建会话时已经落库，本次请求又带了同一条内容，从历史中移除
        if (isInitialMessage && !history.isEmpty()) {
            history.remove(history.size() - 1);
        }

        if (!isInitialMessage) {
            // 保存用户消息到数据库
            consultationMessageService.saveMessage(sessionIdLong, message, null);
        }

        StringBuilder reply = new StringBuilder();
        return chatClient.prompt()
                .messages(history)
                .user(message)
                .stream()
                .content()
                .doOnNext(reply::append)
                .doOnComplete(() -> {
                    if (!reply.isEmpty()) {
                        // 流式结束后保存完整的 AI 回复
                        consultationMessageService.saveAiMessage(sessionIdLong, reply.toString(), AI_MODEL);
                    }
                })
                .doOnError(error -> System.err.println("AI 对话失败: " + error.getMessage()));
    }

    // 获取sessionId
    public Long getSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }

}
