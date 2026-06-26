package com.credit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 签署请求DTO
 */
@Data
public class SignRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 协议版本
     */
    private String agreementVersion;

    /**
     * 签署人ID
     */
    private Long signUserId;

    /**
     * 签署人姓名
     */
    private String signUserName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备信息
     */
    private String deviceInfo;

}
