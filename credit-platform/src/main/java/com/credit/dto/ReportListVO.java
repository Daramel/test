package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告列表VO
 */
@Data
public class ReportListVO implements Serializable {

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
     * 报告状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
