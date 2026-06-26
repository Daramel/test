package com.credit.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 评分趋势VO
 */
@Data
public class ScoreTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 趋势数据列表
     */
    private List<ScoreTrendItem> trendList;

    /**
     * 评分变化（与上次相比）
     */
    private Integer change;

    /**
     * 变化趋势: UP, DOWN, STABLE
     */
    private String trend;

    @Data
    public static class ScoreTrendItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 评估时间
         */
        private String evaluationTime;

        /**
         * 总评分
         */
        private Integer totalScore;

        /**
         * 信用等级
         */
        private String creditRating;
    }

}
