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
 * 企业信息表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_enterprise")
public class SysEnterprise implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    @TableId(type = IdType.AUTO)
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCreditCode;

    /**
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 注册资本
     */
    private BigDecimal registeredCapital;

    /**
     * 实缴资本
     */
    private BigDecimal paidInCapital;

    /**
     * 企业类型
     */
    private String enterpriseType;

    /**
     * 成立日期
     */
    private LocalDate establishedDate;

    /**
     * 营业期限开始
     */
    private LocalDate businessStartDate;

    /**
     * 营业期限结束
     */
    private LocalDate businessEndDate;

    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 注册地址
     */
    private String registeredAddress;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 企业状态: 0-存续, 1-在业, 2-吊销, 3-注销
     */
    private Integer status;

    /**
     * 信用评级
     */
    private String creditRating;

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
