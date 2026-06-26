package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付结果DTO
 */
@Data
public class PayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 支付订单号
     */
    private String orderNo;

    /**
     * 支付跳转URL(扫码支付时返回二维码内容)
     */
    private String payUrl;

    /**
     * 预支付订单号
     */
    private String prepayId;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

}
