package com.yanzz.aispringbootyan.controller;

import com.yanzz.aispringbootyan.DTO.response.AnalyticsOverviewVO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.service.DataAnalyticsService;
import com.yanzz.aispringbootyan.utils.CurrentUserUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {

    @Resource
    private DataAnalyticsService dataAnalyticsService;

    // 管理后台数据看板总览
    @GetMapping("/overview")
    public Result<AnalyticsOverviewVO> overview() {
        CurrentUserUtil.requireAdmin();
        return Result.ok(dataAnalyticsService.getOverview());
    }
}
