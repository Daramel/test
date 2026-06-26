package com.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.credit.entity.AuthAgreement;
import com.credit.mapper.AuthAgreementMapper;
import com.credit.service.AuthAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 授权协议服务实现
 */
@Service
public class AuthAgreementServiceImpl implements AuthAgreementService {

    @Autowired
    private AuthAgreementMapper authAgreementMapper;

    @Override
    public AuthAgreement getLatestAgreement() {
        LambdaQueryWrapper<AuthAgreement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthAgreement::getStatus, 1)
                .orderByDesc(AuthAgreement::getPublishTime)
                .last("LIMIT 1");
        return authAgreementMapper.selectOne(wrapper);
    }

    @Override
    public AuthAgreement getAgreementByVersion(String version) {
        LambdaQueryWrapper<AuthAgreement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthAgreement::getVersion, version)
                .eq(AuthAgreement::getStatus, 1);
        return authAgreementMapper.selectOne(wrapper);
    }

}
