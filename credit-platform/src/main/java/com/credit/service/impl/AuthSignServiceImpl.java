package com.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.dto.SignRequest;
import com.credit.dto.SignStatusVO;
import com.credit.entity.AuthAgreement;
import com.credit.entity.AuthSignRecord;
import com.credit.mapper.AuthSignRecordMapper;
import com.credit.service.AuthAgreementService;
import com.credit.service.AuthSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 授权签署服务实现
 */
@Service
public class AuthSignServiceImpl implements AuthSignService {

    @Autowired
    private AuthSignRecordMapper authSignRecordMapper;

    @Autowired
    private AuthAgreementService authAgreementService;

    @Override
    public SignStatusVO checkSignStatus(Long enterpriseId) {
        SignStatusVO vo = new SignStatusVO();
        vo.setEnterpriseId(enterpriseId);

        AuthSignRecord record = getSignRecord(enterpriseId);
        if (record == null) {
            vo.setHasSigned(false);
            vo.setSignStatus(0);
            vo.setStatusDesc("未签署授权协议");
            return vo;
        }

        vo.setHasSigned(true);
        vo.setSignStatus(record.getSignStatus());
        vo.setSignTime(record.getSignTime());
        vo.setAgreementVersion(record.getTitle());

        switch (record.getSignStatus()) {
            case 1:
                vo.setStatusDesc("已签署授权协议");
                break;
            case 2:
                vo.setStatusDesc("已拒绝授权");
                break;
            case 3:
                vo.setStatusDesc("已撤回授权");
                break;
            default:
                vo.setStatusDesc("待签署");
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSignRecord(SignRequest request) {
        AuthAgreement agreement = authAgreementService.getLatestAgreement();
        if (agreement == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "暂无可用授权协议");
        }

        AuthSignRecord record = new AuthSignRecord();
        record.setEnterpriseId(request.getEnterpriseId());
        record.setSignType(1);
        record.setTitle(agreement.getVersion());
        record.setSignUserId(request.getSignUserId());
        record.setSignUserName(request.getSignUserName());
        record.setSignTime(LocalDateTime.now());
        record.setSignStatus(1);
        record.setResultDesc("授权签署成功");
        record.setIpAddress(request.getIpAddress());
        record.setDeviceInfo(request.getDeviceInfo());

        authSignRecordMapper.insert(record);
        return record.getRecordId();
    }

    @Override
    public AuthSignRecord getSignRecord(Long enterpriseId) {
        LambdaQueryWrapper<AuthSignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSignRecord::getEnterpriseId, enterpriseId)
                .eq(AuthSignRecord::getSignType, 1)
                .orderByDesc(AuthSignRecord::getSignTime)
                .last("LIMIT 1");
        return authSignRecordMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeSign(Long enterpriseId) {
        AuthSignRecord record = getSignRecord(enterpriseId);
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未找到签署记录");
        }

        if (record.getSignStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前状态不允许撤回");
        }

        LambdaUpdateWrapper<AuthSignRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuthSignRecord::getRecordId, record.getRecordId())
                .set(AuthSignRecord::getSignStatus, 3)
                .set(AuthSignRecord::getResultDesc, "授权已撤回");

        return authSignRecordMapper.update(null, wrapper) > 0;
    }

}
