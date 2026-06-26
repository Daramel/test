package com.credit.controller;

import com.credit.common.Result;
import com.credit.dto.CouponListVO;
import com.credit.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/api/coupon")
@Tag(name = "优惠券管理")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    @Operation(summary = "获取我的优惠券")
    public Result<List<CouponListVO>> getUserCoupons(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId) {
        List<CouponListVO> coupons = couponService.getUserCoupons(enterpriseId);
        return Result.success(coupons);
    }

    @PostMapping("/receive")
    @Operation(summary = "领取优惠券")
    public Result<Void> receiveCoupon(@RequestBody Map<String, Long> request) {
        Long enterpriseId = request.get("enterpriseId");
        Long couponId = request.get("couponId");
        couponService.grantCoupon(enterpriseId, couponId);
        return Result.success();
    }

    @GetMapping("/discount")
    @Operation(summary = "计算订单优惠")
    public Result<BigDecimal> calculateDiscount(
            @Parameter(description = "企业ID") @RequestParam Long enterpriseId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        BigDecimal discount = couponService.calculateDiscount(enterpriseId, orderAmount);
        return Result.success(discount);
    }

}
