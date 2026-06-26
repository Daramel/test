package com.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.common.PageResult;
import com.credit.dto.ReportDetailVO;
import com.credit.dto.ReportListVO;
import com.credit.entity.CreditReport;
import com.credit.entity.ReportGrant;
import com.credit.entity.SysEnterprise;
import com.credit.entity.SysUser;
import com.credit.mapper.CreditReportMapper;
import com.credit.mapper.ReportGrantMapper;
import com.credit.mapper.SysEnterpriseMapper;
import com.credit.mapper.SysUserMapper;
import com.credit.service.ReportGeneratorService;
import com.credit.service.ReportService;
import com.credit.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报告Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CreditReportMapper creditReportMapper;
    private final SysEnterpriseMapper sysEnterpriseMapper;
    private final SysUserMapper sysUserMapper;
    private final ReportGrantMapper reportGrantMapper;
    private final ReportGeneratorService reportGeneratorService;

    /**
     * 报告生成进度缓存
     */
    private static final Map<Long, Integer> PROGRESS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, String> STEP_CACHE = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(Long enterpriseId, Long userId, Integer reportType, Long orderId) {
        // 验证企业是否存在
        SysEnterprise enterprise = sysEnterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "企业不存在");
        }

        // 生成报告编号
        String reportNo = generateReportNo();

        // 创建报告记录
        CreditReport report = new CreditReport();
        report.setReportNo(reportNo);
        report.setEnterpriseId(enterpriseId);
        report.setReportType(reportType);
        report.setTitle(getReportTitle(reportType, enterprise.getName()));
        report.setStatus(0); // 生成中
        report.setGenerateDate(LocalDate.now());
        report.setExpireDate(LocalDate.now().plusYears(1));
        report.setCreateUserId(userId);

        if (creditReportMapper.insert(report) <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "创建报告失败");
        }

        // 异步生成报告
        reportGeneratorService.generate(report.getReportId());

        return report.getReportId();
    }

    @Override
    public void generateReport(Long reportId) {
        reportGeneratorService.generate(reportId);
    }

    @Override
    public ReportDetailVO getReportDetail(Long reportId) {
        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "报告不存在");
        }

        SysEnterprise enterprise = sysEnterpriseMapper.selectById(report.getEnterpriseId());

        ReportDetailVO vo = new ReportDetailVO();
        vo.setReportId(report.getReportId());
        vo.setReportNo(report.getReportNo());
        vo.setEnterpriseId(report.getEnterpriseId());
        vo.setEnterpriseName(enterprise != null ? enterprise.getName() : "");
        vo.setUnifiedSocialCreditCode(enterprise != null ? enterprise.getUnifiedSocialCreditCode() : "");
        vo.setReportType(report.getReportType());
        vo.setReportTypeName(getReportTypeName(report.getReportType()));
        vo.setTitle(report.getTitle());
        vo.setCreditScore(report.getCreditScore());
        vo.setCreditLevel(report.getCreditLevel());
        vo.setGenerateDate(report.getGenerateDate());
        vo.setExpireDate(report.getExpireDate());
        vo.setFileUrl(report.getFileUrl());
        vo.setStatus(report.getStatus());
        vo.setStatusName(getStatusName(report.getStatus()));
        vo.setQueryReason(report.getQueryReason());
        vo.setCreateTime(report.getCreateTime());

        return vo;
    }

    @Override
    public PageResult<ReportListVO> getReportList(Long enterpriseId, Integer pageNum, Integer pageSize) {
        Page<CreditReport> page = new Page<>(pageNum, pageSize);
        Page<CreditReport> result = creditReportMapper.selectPageByOwnerEnterpriseId(page, enterpriseId);

        List<ReportListVO> voList = new ArrayList<>();
        for (CreditReport report : result.getRecords()) {
            SysEnterprise enterprise = sysEnterpriseMapper.selectById(report.getEnterpriseId());

            ReportListVO vo = new ReportListVO();
            vo.setReportId(report.getReportId());
            vo.setReportNo(report.getReportNo());
            vo.setEnterpriseId(report.getEnterpriseId());
            vo.setEnterpriseName(enterprise != null ? enterprise.getName() : "");
            vo.setReportType(report.getReportType());
            vo.setReportTypeName(getReportTypeName(report.getReportType()));
            vo.setTitle(report.getTitle());
            vo.setCreditScore(report.getCreditScore());
            vo.setCreditLevel(report.getCreditLevel());
            vo.setGenerateDate(report.getGenerateDate());
            vo.setStatus(report.getStatus());
            vo.setStatusName(getStatusName(report.getStatus()));
            vo.setCreateTime(report.getCreateTime());

            voList.add(vo);
        }

        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public String getReportFileUrl(Long reportId) {
        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "报告不存在");
        }

        if (report.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "报告尚未生成完成");
        }

        return report.getFileUrl();
    }

    @Override
    public boolean checkAccessPermission(Long userId, Long reportId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null) {
            return false;
        }

        // 企业所有者可以访问
        if (user.getEnterpriseId() != null && user.getEnterpriseId().equals(report.getEnterpriseId())) {
            return true;
        }

        // 检查是否是被授权的报告
        List<ReportGrant> grants = reportGrantMapper.selectValidByGranteeEnterpriseId(user.getEnterpriseId());
        for (ReportGrant grant : grants) {
            if (grant.getReportIds() != null && grant.getReportIds().contains(String.valueOf(reportId))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 生成报告编号
     */
    private String generateReportNo() {
        return "CR" + DateUtil.formatLocalDateTime(LocalDateTime.now(), "yyyyMMddHHmmss")
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 获取报告标题
     */
    private String getReportTitle(Integer reportType, String enterpriseName) {
        String typeName = getReportTypeName(reportType);
        return enterpriseName + typeName;
    }

    /**
     * 获取报告类型名称
     */
    private String getReportTypeName(Integer reportType) {
        if (reportType == null) return "";
        return switch (reportType) {
            case 1 -> "企业基础信用报告";
            case 2 -> "企业深度征信报告";
            case 3 -> "企业专项报告";
            default -> "未知类型";
        };
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "生成中";
            case 1 -> "已完成";
            case 2 -> "生成失败";
            default -> "未知状态";
        };
    }

}
