package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建请求DTO
 */
@Data
public class OrderCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单类型: 1-购买报告, 2-充值, 3-订阅服务
     */
    private Integer orderType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单金额
     */
    private BigDecimal totalAmount;

    /**
     * 报告类型: 1-基础报告, 2-深度报告, 3-专项报告
     */
    private Integer reportType;

    /**
     * 目标企业ID(查询报告时指定)
     */
    private Long enterpriseTargetId;

}
