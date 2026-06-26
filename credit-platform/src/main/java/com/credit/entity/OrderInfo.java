package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 产品类型: 1-基础信用报告, 2-深度征信报告, 3-专项报告
     */
    private Integer productType;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-银行转账
     */
    private Integer payMethod;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付状态: 0-待支付, 1-已支付, 2-已取消, 3-已退款
     */
    private Integer payStatus;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 订单状态: 0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    private Integer orderStatus;

    /**
     * 报告ID(完成后关联)
     */
    private Long reportId;

    /**
     * 订单类型: 1-购买报告, 2-充值, 3-订阅服务
     */
    private Integer orderType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 报告类型: 1-基础报告, 2-深度报告, 3-专项报告
     */
    private Integer reportType;

    /**
     * 目标企业ID(查询报告时指定)
     */
    private Long enterpriseTargetId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;

}
