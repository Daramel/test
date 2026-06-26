package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("refund_record")
public class RefundRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款类型: 1-主动退款, 2-自动退款, 3-风控退款
     */
    private Integer refundType;

    /**
     * 退款状态: 0-待审核, 1-审核通过, 2-审核拒绝, 3-已退款, 4-退款失败
     */
    private Integer refundStatus;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
