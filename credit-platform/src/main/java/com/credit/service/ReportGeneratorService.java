package com.credit.service;

import com.credit.entity.CreditReport;

/**
 * 报告生成器接口
 */
public interface ReportGeneratorService {

    /**
     * 生成报告
     *
     * @param reportId 报告ID
     */
    void generate(Long reportId);

    /**
     * 获取生成进度
     *
     * @param reportId 报告ID
     * @return 进度百分比(0-100)
     */
    int getProgress(Long reportId);

    /**
     * 获取当前步骤
     *
     * @param reportId 报告ID
     * @return 步骤描述
     */
    String getCurrentStep(Long reportId);

}
