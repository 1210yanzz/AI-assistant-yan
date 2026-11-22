package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.response.AnalyticsOverviewVO;

public interface DataAnalyticsService {

    // 管理后台数据看板总览
    AnalyticsOverviewVO getOverview();
}
