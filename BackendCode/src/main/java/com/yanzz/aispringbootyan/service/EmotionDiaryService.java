package com.yanzz.aispringbootyan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.EmotionDiaryCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.EmotionDiaryVO;

public interface EmotionDiaryService {

    // 新增或覆盖当天日记：同一用户同一日期已存在时更新，并清空旧的 AI 分析
    EmotionDiaryVO saveDiary(Long userId, EmotionDiaryCommandDTO emotionDiaryCommandDTO);

    // 后台分页查询，可按用户和情绪评分范围筛选
    Page<EmotionDiaryVO> pageDiaries(Long userId, Integer minMoodScore, Integer maxMoodScore, long current, long size);

    // 删除单条日记
    void deleteDiary(Long id);
}
