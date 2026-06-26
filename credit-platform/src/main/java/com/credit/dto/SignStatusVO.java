package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 签署状态VO
 */
@Data
public class SignStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 是否已签署
     */
    private Boolean hasSigned;

    /**
     * 签署状态: 0-待签署, 1-已签署, 2-已拒绝, 3-已撤回
     */
    private Integer signStatus;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 签署时间
     */
    private LocalDateTime signTime;

    /**
     * 协议版本
     */
    private String agreementVersion;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

}
