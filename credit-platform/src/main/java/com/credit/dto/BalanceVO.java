package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 余额信息VO
 */
@Data
public class BalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 当前余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

}
