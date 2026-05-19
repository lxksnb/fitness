package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.DashboardVO;
import com.fitness.service.DashboardService;
import org.springframework.web.bind.annotation.*;

/**
 * 首页看板控制器
 * <p>
 * 提供首页看板聚合数据查询接口，
 * 返回今日体重、饮食、饮水、训练、目标等综合信息。
 */
@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取首页看板数据
     * <p>
     * 一次性返回体重对比、营养目标完成度、饮水进度、
     * 今日训练、连续打卡、体重趋势等看板所需全部数据。
     *
     * @return 首页看板聚合数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.ok(dashboardService.getDashboard());
    }
}
