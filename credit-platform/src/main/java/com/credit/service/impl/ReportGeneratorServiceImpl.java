package com.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.entity.CreditReport;
import com.credit.entity.SysEnterprise;
import com.credit.mapper.CreditReportMapper;
import com.credit.mapper.SysEnterpriseMapper;
import com.credit.service.PdfRenderService;
import com.credit.service.ReportGeneratorService;
import com.credit.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报告生成器Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGeneratorServiceImpl implements ReportGeneratorService {

    private final CreditReportMapper creditReportMapper;
    private final SysEnterpriseMapper sysEnterpriseMapper;
    private final PdfRenderService pdfRenderService;
    private final WatermarkService watermarkService;

    @Value("${report.template.path:classpath:/templates/report/}")
    private String templatePath;

    @Value("${report.storage.path:/data/reports/}")
    private String storagePath;

    @Value("${report.watermark.enabled:true}")
    private boolean watermarkEnabled;

    @Value("${report.watermark.text:仅供内部使用}")
    private String watermarkText;

    /**
     * 进度缓存
     */
    private static final Map<Long, Integer> PROGRESS_MAP = new ConcurrentHashMap<>();
    private static final Map<Long, String> STEP_MAP = new ConcurrentHashMap<>();

    @Override
    public void generate(Long reportId) {
        try {
            updateProgress(reportId, 0, "初始化");
            CreditReport report = creditReportMapper.selectById(reportId);
            if (report == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "报告不存在");
            }

            // 1. 查询企业信息
            updateProgress(reportId, 10, "查询企业信息");
            SysEnterprise enterprise = sysEnterpriseMapper.selectById(report.getEnterpriseId());
            if (enterprise == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "企业不存在");
            }

            // 2. 计算信用评分
            updateProgress(reportId, 30, "计算信用评分");
            int creditScore = calculateCreditScore(enterprise);
            String creditLevel = getCreditLevel(creditScore);

            // 3. 查询风险数据（简化版）
            updateProgress(reportId, 50, "查询风险数据");
            Map<String, Object> riskData = queryRiskData(enterprise);

            // 4. 组装报告数据
            updateProgress(reportId, 60, "组装报告数据");
            Map<String, Object> reportData = buildReportData(report, enterprise, creditScore, creditLevel, riskData);

            // 5. 使用Thymeleaf渲染HTML模板
            updateProgress(reportId, 70, "渲染报告模板");
            String templateName = getTemplateName(report.getReportType());
            InputStream pdfStream = pdfRenderService.renderToPdf(templateName, reportData);

            // 6. 添加水印
            updateProgress(reportId, 80, "添加水印");
            if (watermarkEnabled) {
                pdfStream = watermarkService.addTextWatermark(pdfStream, watermarkText);
            }

            // 7. 上传文件（本地存储）
            updateProgress(reportId, 90, "保存报告文件");
            String fileUrl = saveReportFile(reportId, pdfStream);

            // 8. 更新报告状态
            updateProgress(reportId, 95, "更新报告信息");
            report.setCreditScore(creditScore);
            report.setCreditLevel(creditLevel);
            report.setFileUrl(fileUrl);
            report.setStatus(1); // 已完成
            creditReportMapper.updateById(report);

            updateProgress(reportId, 100, "完成");
            log.info("报告生成完成: reportId={}, creditScore={}, creditLevel={}", reportId, creditScore, creditLevel);

        } catch (BusinessException e) {
            updateReportStatus(reportId, 2, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("报告生成失败: reportId={}", reportId, e);
            updateReportStatus(reportId, 2, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "报告生成失败: " + e.getMessage());
        } finally {
            PROGRESS_MAP.remove(reportId);
            STEP_MAP.remove(reportId);
        }
    }

    @Override
    public int getProgress(Long reportId) {
        return PROGRESS_MAP.getOrDefault(reportId, 0);
    }

    @Override
    public String getCurrentStep(Long reportId) {
        return STEP_MAP.getOrDefault(reportId, "");
    }

    /**
     * 计算信用评分
     */
    private int calculateCreditScore(SysEnterprise enterprise) {
        int baseScore = 70;

        // 企业状态加分
        if (enterprise.getStatus() != null) {
            if (enterprise.getStatus() == 0 || enterprise.getStatus() == 1) {
                baseScore += 10; // 存续或在业
            } else {
                baseScore -= 20; // 吊销或注销
            }
        }

        // 注册资本加分（简化逻辑）
        if (enterprise.getRegisteredCapital() != null) {
            if (enterprise.getRegisteredCapital().compareTo(new java.math.BigDecimal("1000")) > 0) {
                baseScore += 5;
            }
            if (enterprise.getRegisteredCapital().compareTo(new java.math.BigDecimal("5000")) > 0) {
                baseScore += 5;
            }
        }

        // 成立时间加分
        if (enterprise.getEstablishedDate() != null) {
            long years = java.time.temporal.ChronoUnit.YEARS.between(
                    enterprise.getEstablishedDate(), LocalDate.now());
            if (years > 10) {
                baseScore += 10;
            } else if (years > 5) {
                baseScore += 5;
            }
        }

        return Math.min(100, Math.max(0, baseScore));
    }

    /**
     * 根据评分获取信用等级
     */
    private String getCreditLevel(int score) {
        if (score >= 90) return "AAA";
        if (score >= 80) return "AA";
        if (score >= 70) return "A";
        if (score >= 60) return "BBB";
        if (score >= 50) return "BB";
        if (score >= 40) return "B";
        if (score >= 30) return "CCC";
        if (score >= 20) return "CC";
        if (score >= 10) return "C";
        return "D";
    }

    /**
     * 查询风险数据（简化版）
     */
    private Map<String, Object> queryRiskData(SysEnterprise enterprise) {
        Map<String, Object> riskData = new HashMap<>();
        riskData.put("hasRisk", false);
        riskData.put("riskLevel", "低风险");
        riskData.put("riskDesc", "未发现明显风险信息");
        return riskData;
    }

    /**
     * 构建报告数据
     */
    private Map<String, Object> buildReportData(CreditReport report, SysEnterprise enterprise,
                                                 int creditScore, String creditLevel,
                                                 Map<String, Object> riskData) {
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("enterprise", enterprise);
        data.put("creditScore", creditScore);
        data.put("creditLevel", creditLevel);
        data.put("riskData", riskData);
        data.put("generateDate", LocalDate.now());
        data.put("reportTypeName", getReportTypeName(report.getReportType()));
        return data;
    }

    /**
     * 获取模板名称
     */
    private String getTemplateName(Integer reportType) {
        if (reportType == null || reportType == 1) {
            return "simple_v1";
        }
        return "simple_v1";
    }

    /**
     * 保存报告文件
     */
    private String saveReportFile(Long reportId, InputStream inputStream) {
        try {
            // 确保目录存在
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = dir.resolve(fileName);

            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                inputStream.transferTo(outputStream);
            }

            return filePath.toString();
        } catch (Exception e) {
            log.error("保存报告文件失败: reportId={}", reportId, e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "保存报告文件失败");
        }
    }

    /**
     * 更新进度
     */
    private void updateProgress(Long reportId, int progress, String step) {
        PROGRESS_MAP.put(reportId, progress);
        STEP_MAP.put(reportId, step);
    }

    /**
     * 更新报告状态
     */
    private void updateReportStatus(Long reportId, int status, String errorMsg) {
        CreditReport report = creditReportMapper.selectById(reportId);
        if (report != null) {
            report.setStatus(status);
            creditReportMapper.updateById(report);
        }
    }

    private String getReportTypeName(Integer reportType) {
        if (reportType == null) return "";
        return switch (reportType) {
            case 1 -> "企业基础信用报告";
            case 2 -> "企业深度征信报告";
            case 3 -> "企业专项报告";
            default -> "企业信用报告";
        };
    }

}
