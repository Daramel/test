package com.credit.controller;

import cn.hutool.core.bean.BeanUtil;
import com.credit.common.PageResult;
import com.credit.common.Result;
import com.credit.dto.OrderCreateRequest;
import com.credit.dto.OrderListVO;
import com.credit.entity.OrderInfo;
import com.credit.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public Result<OrderInfo> createOrder(@RequestBody OrderCreateRequest request) {
        OrderInfo order = orderService.createOrder(
                request.getEnterpriseId(),
                request.getUserId(),
                request.getOrderType(),
                request.getGoodsName(),
                request.getTotalAmount(),
                request.getReportType(),
                request.getEnterpriseTargetId()
        );
        return Result.success(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<OrderInfo> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        OrderInfo order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    @GetMapping("/list")
    @Operation(summary = "获取订单列表")
    public Result<PageResult<OrderListVO>> getOrderList(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<OrderListVO> result = orderService.getOrderList(enterpriseId, pageNum, pageSize, status);
        return Result.success(result);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        orderService.cancelOrder(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "申请退款")
    public Result<Void> applyRefund(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "退款原因") @RequestParam String reason) {
        orderService.applyRefund(id, userId, reason);
        return Result.success();
    }

}
