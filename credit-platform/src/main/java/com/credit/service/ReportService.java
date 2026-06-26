package com.credit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.common.PageResult;
import com.credit.dto.ReportCreateRequest;
import com.credit.dto.ReportDetailVO;
import com.credit.dto.ReportListVO;

/**
 * 报告Service接口
 */
public interface ReportService {

    /**
     * 创建报告订单
     *
     * @param enterpriseId 企业ID
     * @param userId       用户ID
     * @param reportType   报告类型
     * @param orderId      订单ID
     * @return 报告ID
     */
    Long createReport(Long enterpriseId, Long userId, Integer reportType, Long orderId);

    /**
     * 生成报告（异步）
     *
     * @param reportId 报告ID
     */
    void generateReport(Long reportId);

    /**
     * 获取报告详情
     *
     * @param reportId 报告ID
     * @return 报告详情
     */
    ReportDetailVO getReportDetail(Long reportId);

    /**
     * 获取企业报告列表
     *
     * @param enterpriseId 企业ID
     * @param pageNum      页码
     * @param pageSize     每页数量
     * @return 分页结果
     */
    PageResult<ReportListVO> getReportList(Long enterpriseId, Integer pageNum, Integer pageSize);

    /**
     * 获取报告文件URL
     *
     * @param reportId 报告ID
     * @return 文件URL
     */
    String getReportFileUrl(Long reportId);

    /**
     * 检查报告访问权限
     *
     * @param userId   用户ID
     * @param reportId 报告ID
     * @return 是否有权限
     */
    boolean checkAccessPermission(Long userId, Long reportId);

}
