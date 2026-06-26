package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评分展示VO
 */
@Data
public class ScoreVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 总评分
     */
    private Integer totalScore;

    /**
     * 信用等级
     */
    private String creditRating;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 基础分
     */
    private Integer basicScore;

    /**
     * 经营分
     */
    private Integer operationScore;

    /**
     * 财务分
     */
    private Integer financeScore;

    /**
     * 信用记录分
     */
    private Integer creditRecordScore;

    /**
     * 潜力分
     */
    private Integer potentialScore;

    /**
     * 评估时间
     */
    private LocalDateTime evaluationTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否有效
     */
    private Boolean isValid;

}
