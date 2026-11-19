package com.yanzz.aispringbootyan.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.entity.ConsultationMessage;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.mapper.ConsultationSessionMapper;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Override
    public ConsultationSession createConsultationSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        // 验证用户是否存在
        if (userId != null) {
            // 用户存在，创建会话
            ConsultationSession consultationSession = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(consultationSessionCreateDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            // 如果没有提供标题，则使用默认值
            if (StrUtil.isBlank(consultationSessionCreateDTO.getSessionTitle())) {
                consultationSession.setSessionTitle(String.format("栖心AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd mm:ss")));
            }
            // 插入会话数据
            consultationSessionMapper.insert(consultationSession);
            return consultationSession;
        }
        // 用户不存在，返回null
        return null;
    }
}
