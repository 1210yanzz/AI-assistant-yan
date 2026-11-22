package com.yanzz.aispringbootyan.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationSessionVO;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.entity.User;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.mapper.ConsultationSessionMapper;
import com.yanzz.aispringbootyan.mapper.UserMapper;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Resource
    private ConsultationMessageService consultationMessageService;

    @Resource
    private UserMapper userMapper;

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
                consultationSession.setSessionTitle("栖心AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
            }
            // 插入会话数据
            consultationSessionMapper.insert(consultationSession);
            return consultationSession;
        }
        // 用户不存在，返回null
        return null;
    }

    @Override
    public Page<ConsultationSessionVO> pageSessions(Long userId, boolean admin, long pageNum, long pageSize) {
        Page<ConsultationSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        // 普通用户只能看到自己的会话，管理员可以看全部
        if (!admin) {
            queryWrapper.eq(ConsultationSession::getUserId, userId);
        }
        queryWrapper.orderByDesc(ConsultationSession::getStartedAt)
                .orderByDesc(ConsultationSession::getId);

        Page<ConsultationSession> sessionPage = consultationSessionMapper.selectPage(page, queryWrapper);
        List<ConsultationSession> sessions = sessionPage.getRecords();

        // 批量查询用户昵称，避免列表逐条查用户
        Map<Long, User> userMap = new HashMap<>();
        Set<Long> userIds = sessions.stream()
                .map(ConsultationSession::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
        }

        List<ConsultationSessionVO> records = new ArrayList<>(sessions.size());
        for (ConsultationSession session : sessions) {
            records.add(convertToSessionVO(session, userMap.get(session.getUserId())));
        }

        Page<ConsultationSessionVO> voPage = new Page<>(sessionPage.getCurrent(), sessionPage.getSize(), sessionPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public ConsultationSession getOwnedSession(Long sessionId, Long userId, boolean admin) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "会话不存在");
        }
        if (!admin && !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException(ResultCode.AUTHORIZED_ERROR.getCode(), "无权访问该会话");
        }
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId, Long userId, boolean admin) {
        // 先校验会话存在且属于当前用户（管理员除外）
        getOwnedSession(sessionId, userId, admin);
        // 先删消息再删会话，避免留下孤立消息
        consultationMessageService.deleteMessagesBySessionId(sessionId);
        consultationSessionMapper.deleteById(sessionId);
    }

    @Override
    public void saveEmotionAnalysis(Long sessionId, String analysisJson) {
        LambdaUpdateWrapper<ConsultationSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConsultationSession::getId, sessionId)
                .set(ConsultationSession::getLastEmotionAnalysis, analysisJson)
                .set(ConsultationSession::getLastEmotionUpdatedAt, LocalDateTime.now());
        consultationSessionMapper.update(null, updateWrapper);
    }

    // 把会话实体转换成列表展示对象，补齐最后消息、消息数、时长和用户昵称
    private ConsultationSessionVO convertToSessionVO(ConsultationSession session, User user) {
        ConsultationSessionVO vo = new ConsultationSessionVO();
        vo.setId(session.getId());
        vo.setUserId(session.getUserId());
        vo.setSessionTitle(session.getSessionTitle());
        vo.setStartedAt(session.getStartedAt());
        vo.setUserNickname(user != null ? user.getDisplayName() : "未知用户");

        ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(session.getId());
        if (lastMessage != null) {
            vo.setLastMessageContent(lastMessage.getContent());
            vo.setLastMessageTime(lastMessage.getCreatedAt());
        }

        Integer messageCount = consultationMessageService.getMessageCountBySessionId(session.getId());
        vo.setMessageCount(messageCount != null ? messageCount : 0);

        LocalDateTime endTime = vo.getLastMessageTime() != null ? vo.getLastMessageTime() : LocalDateTime.now();
        long minutes = session.getStartedAt() != null ? Duration.between(session.getStartedAt(), endTime).toMinutes() : 0;
        vo.setDurationMinutes((int) Math.max(minutes, 0));

        return vo;
    }
}
