package com.credit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 报告生成进度VO
 */
@Data
public class ReportProgressVO implements Serializable {

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
     * 进度状态: 0-生成中, 1-已完成, 2-生成失败
     */
    private Integer status;

    /**
     * 进度百分比(0-100)
     */
    private Integer progress;

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 失败原因
     */
    private String errorMessage;

}
