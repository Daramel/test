package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额流水表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("balance_log")
public class BalanceLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 变动类型: 1-充值, 2-消费, 3-退款, 4-冻结, 5-解冻
     */
    private Integer changeType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    /**
     * 变动后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联类型: order-订单, refund-退款, recharge-充值
     */
    private String relatedType;

    /**
     * 关联ID
     */
    private String relatedId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
