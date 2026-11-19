package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface ConsultationSessionService {

    ConsultationSession createConsultationSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO);
}
