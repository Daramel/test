package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 征信报告表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("credit_report")
public class CreditReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    @TableId(type = IdType.AUTO)
    private Long reportId;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 报告编号
     */
    private String reportNo;

    /**
     * 报告类型: 1-企业基础信用报告, 2-企业深度征信报告, 3-企业专项报告
     */
    private Integer reportType;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 信用评分(0-100)
     */
    private Integer creditScore;

    /**
     * 信用等级: AAA, AA, A, BBB, BB, B, CCC, CC, C, D
     */
    private String creditLevel;

    /**
     * 生成日期
     */
    private LocalDate generateDate;

    /**
     * 报告有效期
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
     * 查询原因
     */
    private String queryReason;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;

}
