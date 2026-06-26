package com.credit.service;

import com.credit.dto.SignRequest;
import com.credit.dto.SignStatusVO;
import com.credit.entity.AuthSignRecord;

/**
 * 授权签署服务接口
 */
public interface AuthSignService {

    /**
     * 检查企业是否已签署授权
     * @param enterpriseId 企业ID
     * @return 签署状态
     */
    SignStatusVO checkSignStatus(Long enterpriseId);

    /**
     * 创建签署记录
     * @param record 签署记录
     * @return 签署记录ID
     */
    Long createSignRecord(SignRequest record);

    /**
     * 获取企业最新签署记录
     * @param enterpriseId 企业ID
     * @return 签署记录
     */
    AuthSignRecord getSignRecord(Long enterpriseId);

    /**
     * 撤回授权
     * @param enterpriseId 企业ID
     * @return 是否成功
     */
    boolean revokeSign(Long enterpriseId);

}
