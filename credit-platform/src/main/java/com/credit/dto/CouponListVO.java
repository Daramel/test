package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券列表VO
 */
@Data
public class CouponListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID
     */
    private Long id;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型: 1-满减, 2-折扣, 3-直减
     */
    private Integer couponType;

    /**
     * 面值
     */
    private BigDecimal denomination;

    /**
     * 使用门槛金额
     */
    private BigDecimal thresholdAmount;

    /**
     * 状态: 0-未使用, 1-已使用, 2-已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

}
