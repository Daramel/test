package com.credit.controller;

import com.credit.common.PageResult;
import com.credit.common.Result;
import com.credit.dto.BalanceVO;
import com.credit.entity.BalanceLog;
import com.credit.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 余额控制器
 */
@RestController
@RequestMapping("/api/balance")
@Tag(name = "余额管理")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @GetMapping
    @Operation(summary = "获取余额信息")
    public Result<BalanceVO> getBalance(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId) {
        BalanceVO balance = balanceService.getBalance(enterpriseId);
        return Result.success(balance);
    }

    @PostMapping("/recharge")
    @Operation(summary = "余额充值")
    public Result<Void> recharge(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId,
            @Parameter(description = "充值金额") @RequestParam BigDecimal amount,
            @Parameter(description = "支付方式") @RequestParam String payMethod) {
        balanceService.recharge(enterpriseId, amount, payMethod);
        return Result.success();
    }

    @GetMapping("/log")
    @Operation(summary = "获取余额流水")
    public Result<PageResult<BalanceLog>> getBalanceLog(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<BalanceLog> result = balanceService.getBalanceLog(enterpriseId, pageNum, pageSize);
        return Result.success(result);
    }

}
