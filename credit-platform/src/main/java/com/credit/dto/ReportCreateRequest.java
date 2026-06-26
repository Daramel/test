package com.credit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 报告创建请求
 */
@Data
public class ReportCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    @NotNull(message = "企业ID不能为空")
    private Long enterpriseId;

    /**
     * 报告类型: 1-企业基础信用报告, 2-企业深度征信报告, 3-企业专项报告
     */
    @NotNull(message = "报告类型不能为空")
    private Integer reportType;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 查询原因
     */
    private String queryReason;

}
