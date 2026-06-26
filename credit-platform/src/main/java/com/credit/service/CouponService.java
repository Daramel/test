package com.credit.service;

import com.credit.dto.CouponListVO;
import com.credit.entity.Coupon;
import com.credit.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 发放优惠券
     * @param enterpriseId 企业ID
     * @param couponId 优惠券ID
     */
    void grantCoupon(Long enterpriseId, Long couponId);

    /**
     * 获取用户优惠券
     * @param enterpriseId 企业ID
     * @return 优惠券列表
     */
    List<CouponListVO> getUserCoupons(Long enterpriseId);

    /**
     * 使用优惠券
     * @param enterpriseId 企业ID
     * @param couponId 优惠券ID
     * @param orderId 订单ID
     * @return 使用结果
     */
    CouponUseResult useCoupon(Long enterpriseId, Long couponId, Long orderId);

    /**
     * 计算订单优惠
     * @param enterpriseId 企业ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    BigDecimal calculateDiscount(Long enterpriseId, BigDecimal orderAmount);

    /**
     * 获取优惠券详情
     * @param couponId 优惠券ID
     * @return 优惠券信息
     */
    Coupon getCouponById(Long couponId);

    /**
     * 优惠券使用结果
     */
    class CouponUseResult {
        private boolean success;
        private BigDecimal discountAmount;
        private String errorMessage;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

}
