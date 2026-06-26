package com.credit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付请求DTO
 */
@Data
public class PayRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付方式: 1-微信, 2-支付宝, 3-余额, 4-银行转账
     */
    private Integer payMethod;

}
