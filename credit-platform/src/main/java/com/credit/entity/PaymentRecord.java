package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("payment_record")
public class PaymentRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-余额, 4-银行转账
     */
    private Integer payMethod;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 支付状态: 0-待支付, 1-已支付, 2-支付失败, 3-已取消
     */
    private Integer payStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 回调数据(JSON)
     */
    private String callbackData;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
