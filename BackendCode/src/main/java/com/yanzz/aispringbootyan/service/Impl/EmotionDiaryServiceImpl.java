package com.yanzz.aispringbootyan.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.EmotionDiaryCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.EmotionDiaryVO;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.entity.EmotionDiary;
import com.yanzz.aispringbootyan.entity.User;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.mapper.EmotionDiaryMapper;
import com.yanzz.aispringbootyan.mapper.UserMapper;
import com.yanzz.aispringbootyan.service.EmotionDiaryService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmotionDiaryServiceImpl implements EmotionDiaryService {

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmotionDiaryVO saveDiary(Long userId, EmotionDiaryCommandDTO emotionDiaryCommandDTO) {
        EmotionDiary existing = selectByUserAndDate(userId, emotionDiaryCommandDTO.getDiaryDate());

        if (existing != null) {
            // 同一天已有日记，直接覆盖，并清空过期的 AI 分析
            updateDiary(existing.getId(), emotionDiaryCommandDTO);
        } else {
            try {
                EmotionDiary diary = EmotionDiary.builder()
                        .userId(userId)
                        .diaryDate(emotionDiaryCommandDTO.getDiaryDate())
                        .moodScore(emotionDiaryCommandDTO.getMoodScore())
                        .dominantEmotion(emotionDiaryCommandDTO.getDominantEmotion())
                        .emotionTriggers(emotionDiaryCommandDTO.getEmotionTriggers())
                        .diaryContent(emotionDiaryCommandDTO.getDiaryContent())
                        .sleepQuality(emotionDiaryCommandDTO.getSleepQuality())
                        .stressLevel(emotionDiaryCommandDTO.getStressLevel())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                emotionDiaryMapper.insert(diary);
            } catch (DuplicateKeyException e) {
                // 并发提交同一日期时由唯一约束兜底，转为覆盖更新
                EmotionDiary concurrent = selectByUserAndDate(userId, emotionDiaryCommandDTO.getDiaryDate());
                if (concurrent == null) {
                    throw e;
                }
                updateDiary(concurrent.getId(), emotionDiaryCommandDTO);
            }
        }

        EmotionDiary saved = selectByUserAndDate(userId, emotionDiaryCommandDTO.getDiaryDate());
        if (saved == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "日记保存失败");
        }
        return convertToVO(saved, userMapper.selectById(userId));
    }

    @Override
    public Page<EmotionDiaryVO> pageDiaries(Long userId, Integer minMoodScore, Integer maxMoodScore,
                                            long current, long size) {
        Page<EmotionDiary> page = new Page<>(current, size);
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(userId != null, EmotionDiary::getUserId, userId)
                .ge(minMoodScore != null, EmotionDiary::getMoodScore, minMoodScore)
                .le(maxMoodScore != null, EmotionDiary::getMoodScore, maxMoodScore)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId);

        Page<EmotionDiary> diaryPage = emotionDiaryMapper.selectPage(page, queryWrapper);
        List<EmotionDiary> diaries = diaryPage.getRecords();
        Map<Long, User> userMap = loadUsers(diaries);

        List<EmotionDiaryVO> records = diaries.stream()
                .map(diary -> convertToVO(diary, userMap.get(diary.getUserId())))
                .collect(Collectors.toList());

        Page<EmotionDiaryVO> voPage = new Page<>(diaryPage.getCurrent(), diaryPage.getSize(), diaryPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public void deleteDiary(Long id) {
        int deleted = emotionDiaryMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "日记不存在");
        }
    }

    private EmotionDiary selectByUserAndDate(Long userId, LocalDate diaryDate) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmotionDiary::getUserId, userId)
                .eq(EmotionDiary::getDiaryDate, diaryDate);
        return emotionDiaryMapper.selectOne(queryWrapper);
    }

    private void updateDiary(Long id, EmotionDiaryCommandDTO emotionDiaryCommandDTO) {
        // updateById 默认忽略 null 字段，这里必须用 UpdateWrapper 显式把 AI 分析两列置空
        LambdaUpdateWrapper<EmotionDiary> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EmotionDiary::getId, id)
                .set(EmotionDiary::getMoodScore, emotionDiaryCommandDTO.getMoodScore())
                .set(EmotionDiary::getDominantEmotion, emotionDiaryCommandDTO.getDominantEmotion())
                .set(EmotionDiary::getEmotionTriggers, emotionDiaryCommandDTO.getEmotionTriggers())
                .set(EmotionDiary::getDiaryContent, emotionDiaryCommandDTO.getDiaryContent())
                .set(EmotionDiary::getSleepQuality, emotionDiaryCommandDTO.getSleepQuality())
                .set(EmotionDiary::getStressLevel, emotionDiaryCommandDTO.getStressLevel())
                .set(EmotionDiary::getAiEmotionAnalysis, null)
                .set(EmotionDiary::getAiAnalysisUpdatedAt, null)
                .set(EmotionDiary::getUpdatedAt, LocalDateTime.now());
        emotionDiaryMapper.update(null, updateWrapper);
    }

    private Map<Long, User> loadUsers(List<EmotionDiary> diaries) {
        Set<Long> userIds = diaries.stream()
                .map(EmotionDiary::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
    }

    private EmotionDiaryVO convertToVO(EmotionDiary diary, User user) {
        EmotionDiaryVO vo = new EmotionDiaryVO();
        vo.setId(diary.getId());
        vo.setUserId(diary.getUserId());
        vo.setNickname(user != null ? user.getDisplayName() : "未知用户");
        vo.setUsername(user != null ? user.getUsername() : "");
        vo.setDiaryDate(diary.getDiaryDate());
        vo.setMoodScore(diary.getMoodScore());
        vo.setDominantEmotion(diary.getDominantEmotion());
        vo.setEmotionTriggers(diary.getEmotionTriggers());
        vo.setDiaryContent(diary.getDiaryContent());
        vo.setSleepQuality(diary.getSleepQuality());
        vo.setStressLevel(diary.getStressLevel());
        vo.setAiEmotionAnalysis(diary.getAiEmotionAnalysis());
        vo.setCreatedAt(diary.getCreatedAt());
        vo.setUpdatedAt(diary.getUpdatedAt());
        return vo;
    }
}
