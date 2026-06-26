package com.credit.controller;

import com.credit.common.Result;
import com.credit.dto.PayRequest;
import com.credit.dto.PayResult;
import com.credit.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/pay")
@Tag(name = "支付管理")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "发起支付")
    public Result<PayResult> initiatePay(@RequestBody PayRequest request) {
        PayResult result = paymentService.initiatePay(request.getOrderId(), request.getPayMethod());
        return Result.success(result);
    }

    @GetMapping("/{orderNo}/status")
    @Operation(summary = "查询支付状态")
    public Result<Integer> queryPayStatus(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Integer status = paymentService.queryPayStatus(orderNo);
        return Result.success(status);
    }

    @PostMapping("/wechat/notify")
    @Operation(summary = "微信支付回调")
    public Result<String> wechatNotify(@RequestBody Map<String, String> params) {
        paymentService.handleWechatCallback(params);
        return Result.success("SUCCESS");
    }

    @PostMapping("/alipay/notify")
    @Operation(summary = "支付宝回调")
    public Result<String> alipayNotify(@RequestBody Map<String, String> params) {
        paymentService.handleAlipayCallback(params);
        return Result.success("success");
    }

    @PostMapping("/balance")
    @Operation(summary = "余额支付")
    public Result<Boolean> balancePay(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId) {
        boolean result = paymentService.balancePay(orderId, enterpriseId);
        return Result.success(result);
    }

}
