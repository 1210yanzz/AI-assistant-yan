package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

import java.util.List;

// 返回给前端展示：数据总览页的返回对象（管理后台仪表盘数据），包含系统概况、情绪趋势、咨询统计、用户活跃度
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class AnalyticsOverviewVO {
    // 系统概况：用户数、日记数、会话数等总览数字
    private SystemOverviewVO systemOverview;

    // 情绪趋势：一段时间内每天的平均心情分
    // List<EmotionTrendVO> 是 Java 泛型集合，List 是有序可重复的列表接口，<> 里指定元素类型为 EmotionTrendVO
    private List<EmotionTrendVO> emotionTrend;

    // 咨询统计：总会话数、平均时长、每日趋势
    private ConsultationStatsVO consultationStats;

    // 用户活跃度：每天活跃/新增/写日记/咨询的人数
    // List<UserActivityVO> 泛型集合，元素类型为 UserActivityVO
    private List<UserActivityVO> userActivity;

    // 系统整体概况静态内部类
    // @Data Lombok 注解，同样自动生成 getter/setter
    @Data
    public static class SystemOverviewVO {
        // 用户总数
        // Long 是 long 的包装类型，适合大整数计数，可表示的范围比 int 大
        private Long totalUsers;
        // 活跃用户数
        private Long activeUsers;
        // 日记总数
        private Long totalDiaries;
        // 今天新增日记数
        private Long todayNewDiaries;
        // 咨询会话总数
        private Long totalSessions;
        // 今天新增会话数
        private Long todayNewSessions;
        // 平均情绪评分
        // Double 是 double 的包装类型，适合存小数
        private Double avgMoodScore;
    }

    // 情绪趋势里的一天数据点静态内部类
    // @Data Lombok 注解
    @Data
    public static class EmotionTrendVO {
        // 日期
        private String date;
        // 当天平均情绪评分
        private Double avgMoodScore;
        // 当天记录的日记条数
        private Long recordCount;
    }

    // 咨询统计汇总静态内部类
    // @Data Lombok 注解
    @Data
    public static class ConsultationStatsVO {
        // 会话总数
        private Long totalSessions;
        // 平均会话时长（分钟）
        private Double avgDurationMinutes;
        // 每天的趋势明细
        // List<DailyTrendVO> 泛型集合
        private List<DailyTrendVO> dailyTrend;
    }

    // 咨询的每日趋势数据点静态内部类
    // @Data Lombok 注解
    @Data
    public static class DailyTrendVO {
        // 日期
        private String date;
        // 当天的会话数量
        private Long sessionCount;
        // 当天参与咨询的用户数
        private Long userCount;
    }

    // 用户活跃度的一天数据点静态内部类
    // @Data Lombok 注解
    @Data
    public static class UserActivityVO {
        // 日期
        private String date;
        // 活跃用户数
        private Long activeUsers;
        // 新增用户数
        private Long newUsers;
        // 写日记的用户数
        private Long diaryUsers;
        // 发起咨询的用户数
        private Long consultationUsers;
    }
}