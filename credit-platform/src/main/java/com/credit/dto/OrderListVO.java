package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表VO
 */
@Data
public class OrderListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-余额, 4-银行转账
     */
    private Integer payMethod;

    /**
     * 支付状态: 0-待支付, 1-已支付, 2-已取消, 3-已退款
     */
    private Integer payStatus;

    /**
     * 订单状态: 0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    private Integer orderStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

}
