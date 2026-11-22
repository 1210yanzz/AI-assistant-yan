package com.yanzz.aispringbootyan.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.mapper.ConsultationMessageMapper;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import jakarta.annotation.Resource;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    @Resource
    private ConsultationMessageMapper consultationMessageMapper;

    @Override
    public ConsultationMessage saveMessage(Long sessionId, String initialMessage, String emotionTag) {
        // 构建用户消息实体
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1)
                .messageType(1)
                .content(initialMessage)
                .emotionTag(emotionTag)
                .createdAt(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(userMessage);
        return userMessage;
    }

    @Override
    public ConsultationMessage saveAiMessage(Long sessionId, String content, String aiModel) {
        // 构建AI回复消息实体
        ConsultationMessage aiMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2)
                .messageType(1)
                .content(content)
                .aiModel(aiModel)
                .createdAt(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(aiMessage);
        return aiMessage;
    }

    @Override
    public Integer getMessageCountBySessionId(Long sessionIdLong) {
        // 构建查询条件
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionIdLong);
        return Math.toIntExact(consultationMessageMapper.selectCount(queryWrapper));
    }

    @Override
    public ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionIdLong) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ConsultationMessage::getSessionId, sessionIdLong)
                .orderByDesc(ConsultationMessage::getCreatedAt)
                .last("limit 1");  //按创建时间降序排列，获取时间最大的数据离当前时间最近，即为最后一条消息
        ConsultationMessage lastMessage = consultationMessageMapper.selectOne(queryWrapper);
        return lastMessage != null ? convertToConsultationMessageResponseDTO(lastMessage) : null;
    }

    @Override
    public List<ConsultationMessage> listRecentMessages(Long sessionId, int limit) {
        // 按创建时间倒序取最近 limit 条，再反转为正序，供 AI 上下文使用
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getCreatedAt)
                .last("limit " + limit);
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<ConsultationMessageResponseDTO> listMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ConsultationMessage::getSessionId, sessionId)
                .orderByAsc(ConsultationMessage::getCreatedAt)
                .orderByAsc(ConsultationMessage::getId);
        return consultationMessageMapper.selectList(queryWrapper)
                .stream()
                .map(this::convertToConsultationMessageResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        consultationMessageMapper.delete(queryWrapper);
    }

    private ConsultationMessageResponseDTO convertToConsultationMessageResponseDTO(ConsultationMessage consultationMessage) {
        if(consultationMessage == null) {
            return null;
        }
        ConsultationMessageResponseDTO consultationMessageResponseDTO = new ConsultationMessageResponseDTO();

        consultationMessageResponseDTO.setId(consultationMessage.getId());
        consultationMessageResponseDTO.setSessionId(consultationMessage.getSessionId());
        consultationMessageResponseDTO.setSenderType(consultationMessage.getSenderType());
        consultationMessageResponseDTO.setMessageType(consultationMessage.getMessageType());
        consultationMessageResponseDTO.setContent(consultationMessage.getContent());
        consultationMessageResponseDTO.setEmotionTag(consultationMessage.getEmotionTag());
        consultationMessageResponseDTO.setAiModel(consultationMessage.getAiModel());
        consultationMessageResponseDTO.setCreatedAt(consultationMessage.getCreatedAt());
        consultationMessageResponseDTO.setSenderTypeDesc(consultationMessage.getSenderTypeDesc());
        consultationMessageResponseDTO.setMessageTypeDesc(consultationMessage.getMessageTypeDesc());
        consultationMessageResponseDTO.calculateContentLength();

        return consultationMessageResponseDTO;
    }
}
