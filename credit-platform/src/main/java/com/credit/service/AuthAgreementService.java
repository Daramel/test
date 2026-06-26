package com.credit.service;

import com.credit.entity.AuthAgreement;

/**
 * 授权协议服务接口
 */
public interface AuthAgreementService {

    /**
     * 获取最新有效协议
     * @return 最新有效协议
     */
    AuthAgreement getLatestAgreement();

    /**
     * 根据版本获取协议
     * @param version 协议版本
     * @return 协议信息
     */
    AuthAgreement getAgreementByVersion(String version);

}
