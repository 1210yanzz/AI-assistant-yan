package com.yanzz.aispringbootyan.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yanzz.aispringbootyan.DTO.response.AnalyticsOverviewVO;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.entity.EmotionDiary;
import com.yanzz.aispringbootyan.entity.User;
import com.yanzz.aispringbootyan.mapper.ConsultationSessionMapper;
import com.yanzz.aispringbootyan.mapper.EmotionDiaryMapper;
import com.yanzz.aispringbootyan.mapper.UserMapper;
import com.yanzz.aispringbootyan.service.DataAnalyticsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DataAnalyticsServiceImpl implements DataAnalyticsService {

    // 看板趋势统一看最近 7 天（含今天）
    private static final int TREND_DAYS = 7;

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Override
    public AnalyticsOverviewVO getOverview() {
        AnalyticsOverviewVO overview = new AnalyticsOverviewVO();
        overview.setSystemOverview(buildSystemOverview());
        overview.setEmotionTrend(buildEmotionTrend());
        overview.setConsultationStats(buildConsultationStats());
        overview.setUserActivity(buildUserActivity());
        return overview;
    }

    private AnalyticsOverviewVO.SystemOverviewVO buildSystemOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        AnalyticsOverviewVO.SystemOverviewVO overview = new AnalyticsOverviewVO.SystemOverviewVO();
        overview.setTotalUsers(userMapper.selectCount(null));
        overview.setActiveUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        overview.setTotalDiaries(emotionDiaryMapper.selectCount(null));
        overview.setTodayNewDiaries(emotionDiaryMapper.selectCount(
                new LambdaQueryWrapper<EmotionDiary>()
                        .ge(EmotionDiary::getCreatedAt, todayStart)
                        .lt(EmotionDiary::getCreatedAt, tomorrowStart)));
        overview.setTotalSessions(consultationSessionMapper.selectCount(null));
        overview.setTodayNewSessions(consultationSessionMapper.selectCount(
                new LambdaQueryWrapper<ConsultationSession>()
                        .ge(ConsultationSession::getStartedAt, todayStart)
                        .lt(ConsultationSession::getStartedAt, tomorrowStart)));
        overview.setAvgMoodScore(selectAvgMoodScore());
        return overview;
    }

    // 整体平均情绪分，没有日记时返回 0
    private Double selectAvgMoodScore() {
        QueryWrapper<EmotionDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("AVG(mood_score)");
        List<Object> values = emotionDiaryMapper.selectObjs(queryWrapper);
        if (values.isEmpty() || values.get(0) == null) {
            return 0.0;
        }
        return round1(((Number) values.get(0)).doubleValue());
    }

    private List<AnalyticsOverviewVO.EmotionTrendVO> buildEmotionTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1);

        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(
                new LambdaQueryWrapper<EmotionDiary>()
                        .select(EmotionDiary::getDiaryDate, EmotionDiary::getMoodScore)
                        .ge(EmotionDiary::getDiaryDate, startDate)
                        .le(EmotionDiary::getDiaryDate, today));

        Map<LocalDate, List<Integer>> scoresByDate = new HashMap<>();
        for (EmotionDiary diary : diaries) {
            if (diary.getDiaryDate() == null || diary.getMoodScore() == null) {
                continue;
            }
            scoresByDate.computeIfAbsent(diary.getDiaryDate(), key -> new ArrayList<>())
                    .add(diary.getMoodScore());
        }

        List<AnalyticsOverviewVO.EmotionTrendVO> trend = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate date = startDate.plusDays(i);
            List<Integer> scores = scoresByDate.getOrDefault(date, List.of());
            AnalyticsOverviewVO.EmotionTrendVO item = new AnalyticsOverviewVO.EmotionTrendVO();
            item.setDate(date.toString());
            item.setRecordCount((long) scores.size());
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            item.setAvgMoodScore(round1(avg));
            trend.add(item);
        }
        return trend;
    }

    private AnalyticsOverviewVO.ConsultationStatsVO buildConsultationStats() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        AnalyticsOverviewVO.ConsultationStatsVO stats = new AnalyticsOverviewVO.ConsultationStatsVO();
        stats.setTotalSessions(consultationSessionMapper.selectCount(null));

        Double avgDuration = consultationSessionMapper.selectAvgDurationMinutes();
        stats.setAvgDurationMinutes(avgDuration != null ? round1(avgDuration) : 0.0);

        List<ConsultationSession> sessions = consultationSessionMapper.selectList(
                new LambdaQueryWrapper<ConsultationSession>()
                        .select(ConsultationSession::getStartedAt, ConsultationSession::getUserId)
                        .ge(ConsultationSession::getStartedAt, start)
                        .lt(ConsultationSession::getStartedAt, end));

        Map<LocalDate, Long> sessionCountByDate = new HashMap<>();
        Map<LocalDate, Set<Long>> userSetsByDate = new HashMap<>();
        for (ConsultationSession session : sessions) {
            if (session.getStartedAt() == null) {
                continue;
            }
            LocalDate date = session.getStartedAt().toLocalDate();
            sessionCountByDate.merge(date, 1L, Long::sum);
            if (session.getUserId() != null) {
                userSetsByDate.computeIfAbsent(date, key -> new HashSet<>()).add(session.getUserId());
            }
        }

        List<AnalyticsOverviewVO.DailyTrendVO> dailyTrend = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate date = startDate.plusDays(i);
            AnalyticsOverviewVO.DailyTrendVO item = new AnalyticsOverviewVO.DailyTrendVO();
            item.setDate(date.toString());
            item.setSessionCount(sessionCountByDate.getOrDefault(date, 0L));
            item.setUserCount((long) userSetsByDate.getOrDefault(date, Set.of()).size());
            dailyTrend.add(item);
        }
        stats.setDailyTrend(dailyTrend);
        return stats;
    }

    private List<AnalyticsOverviewVO.UserActivityVO> buildUserActivity() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // 每天新增用户数
        List<User> newUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .select(User::getCreatedAt)
                        .ge(User::getCreatedAt, start)
                        .lt(User::getCreatedAt, end));
        Map<LocalDate, Long> newUserCountByDate = new HashMap<>();
        for (User user : newUsers) {
            if (user.getCreatedAt() != null) {
                newUserCountByDate.merge(user.getCreatedAt().toLocalDate(), 1L, Long::sum);
            }
        }

        // 每天写日记的去重用户
        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(
                new LambdaQueryWrapper<EmotionDiary>()
                        .select(EmotionDiary::getCreatedAt, EmotionDiary::getUserId)
                        .ge(EmotionDiary::getCreatedAt, start)
                        .lt(EmotionDiary::getCreatedAt, end));
        Map<LocalDate, Set<Long>> diaryUserSets = new HashMap<>();
        for (EmotionDiary diary : diaries) {
            if (diary.getCreatedAt() != null && diary.getUserId() != null) {
                diaryUserSets.computeIfAbsent(diary.getCreatedAt().toLocalDate(), key -> new HashSet<>())
                        .add(diary.getUserId());
            }
        }

        // 每天发起咨询的去重用户
        List<ConsultationSession> sessions = consultationSessionMapper.selectList(
                new LambdaQueryWrapper<ConsultationSession>()
                        .select(ConsultationSession::getStartedAt, ConsultationSession::getUserId)
                        .ge(ConsultationSession::getStartedAt, start)
                        .lt(ConsultationSession::getStartedAt, end));
        Map<LocalDate, Set<Long>> consultationUserSets = new HashMap<>();
        for (ConsultationSession session : sessions) {
            if (session.getStartedAt() != null && session.getUserId() != null) {
                consultationUserSets.computeIfAbsent(session.getStartedAt().toLocalDate(), key -> new HashSet<>())
                        .add(session.getUserId());
            }
        }

        List<AnalyticsOverviewVO.UserActivityVO> activity = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate date = startDate.plusDays(i);
            Set<Long> diaryUsers = diaryUserSets.getOrDefault(date, Set.of());
            Set<Long> consultationUsers = consultationUserSets.getOrDefault(date, Set.of());
            // 活跃用户 = 当天写日记或发起咨询的去重用户
            Set<Long> activeUsers = new HashSet<>();
            activeUsers.addAll(diaryUsers);
            activeUsers.addAll(consultationUsers);

            AnalyticsOverviewVO.UserActivityVO item = new AnalyticsOverviewVO.UserActivityVO();
            item.setDate(date.toString());
            item.setActiveUsers((long) activeUsers.size());
            item.setNewUsers(newUserCountByDate.getOrDefault(date, 0L));
            item.setDiaryUsers((long) diaryUsers.size());
            item.setConsultationUsers((long) consultationUsers.size());
            activity.add(item);
        }
        return activity;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
