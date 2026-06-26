package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("coupon")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 有效天数
     */
    private Integer validDays;

    /**
     * 发行总量
     */
    private Integer totalCount;

    /**
     * 已使用数量
     */
    private Integer usedCount;

    /**
     * 状态: 0-未发布, 1-已发布, 2-已下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
