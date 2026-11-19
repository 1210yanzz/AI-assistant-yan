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
