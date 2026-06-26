package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报告授权表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("report_grant")
public class ReportGrant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 授权ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 授权编号
     */
    private String grantNo;

    /**
     * 授权方企业ID
     */
    private Long grantEnterpriseId;

    /**
     * 被授权方企业ID
     */
    private Long granteeEnterpriseId;

    /**
     * 授权范围: 1-全部, 2-指定
     */
    private Integer grantScope;

    /**
     * 报告ID列表(JSON数组)
     */
    private String reportIds;

    /**
     * 授权时间
     */
    private LocalDateTime grantTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态: 0-已撤销, 1-有效
     */
    private Integer status;

    /**
     * 撤销时间
     */
    private LocalDateTime revokeTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
