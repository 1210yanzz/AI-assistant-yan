package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface ConsultationMessageService {

    ConsultationMessage saveMessage(Long sessionId, @NotBlank(message = "初始消息不能为空") @Size(max = 2000, message = "初始消息最多2000个字符") String initialMessage, String emotionTag);

    Integer getMessageCountBySessionId(Long sessionIdLong);

    ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionIdLong);
}
