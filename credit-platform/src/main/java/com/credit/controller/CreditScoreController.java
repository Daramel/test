package com.credit.controller;

import com.credit.common.Result;
import com.credit.dto.ScoreTrendVO;
import com.credit.dto.ScoreVO;
import com.credit.service.CreditScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 信用评分控制器
 */
@RestController
@RequestMapping("/api/score")
@Tag(name = "信用评分管理")
public class CreditScoreController {

    @Autowired
    private CreditScoreService creditScoreService;

    @GetMapping("/{enterpriseId}")
    @Operation(summary = "获取企业评分")
    public Result<ScoreVO> getScore(@PathVariable Long enterpriseId) {
        ScoreVO score = creditScoreService.getLatestScore(enterpriseId);
        return Result.success(score);
    }

    @GetMapping("/{enterpriseId}/trend")
    @Operation(summary = "获取评分趋势")
    public Result<ScoreTrendVO> getScoreTrend(
            @PathVariable Long enterpriseId,
            @RequestParam(defaultValue = "6") int months) {
        ScoreTrendVO trend = creditScoreService.getScoreTrend(enterpriseId, months);
        return Result.success(trend);
    }

    @PostMapping("/calculate")
    @Operation(summary = "重新计算评分（管理员）")
    public Result<ScoreVO> calculateScore(@RequestParam Long enterpriseId) {
        ScoreVO score = creditScoreService.calculateScore(enterpriseId);
        return Result.success(score);
    }

}
