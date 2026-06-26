package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告详情VO
 */
@Data
public class ReportDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private Long reportId;

    /**
     * 报告编号
     */
    private String reportNo;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCreditCode;

    /**
     * 报告类型
     */
    private Integer reportType;

    /**
     * 报告类型名称
     */
    private String reportTypeName;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 信用评分
     */
    private Integer creditScore;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 生成日期
     */
    private LocalDate generateDate;

    /**
     * 有效期至
     */
    private LocalDate expireDate;

    /**
     * 报告文件URL
     */
    private String fileUrl;

    /**
     * 报告状态: 0-生成中, 1-已完成, 2-生成失败
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 查询原因
     */
    private String queryReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
