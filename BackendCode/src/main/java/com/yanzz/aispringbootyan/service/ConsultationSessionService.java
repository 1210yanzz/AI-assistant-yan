package com.yanzz.aispringbootyan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationSessionVO;
import com.yanzz.aispringbootyan.entity.ConsultationSession;

public interface ConsultationSessionService {

    ConsultationSession createConsultationSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO);

    // 分页查询会话：普通用户只查自己的，管理员查全部
    Page<ConsultationSessionVO> pageSessions(Long userId, boolean admin, long pageNum, long pageSize);

    // 查询会话并校验归属：会话不存在或无权访问时抛 BusinessException
    ConsultationSession getOwnedSession(Long sessionId, Long userId, boolean admin);

    // 删除会话及其全部消息
    void deleteSession(Long sessionId, Long userId, boolean admin);

    // 保存会话情绪分析结果（JSON）和更新时间
    void saveEmotionAnalysis(Long sessionId, String analysisJson);
}
