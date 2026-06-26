package com.credit.service;

import com.credit.dto.ScoreTrendVO;
import com.credit.dto.ScoreVO;

/**
 * 信用评分服务接口
 */
public interface CreditScoreService {

    /**
     * 计算信用评分（简化版实现）
     * @param enterpriseId 企业ID
     * @return 评分VO
     */
    ScoreVO calculateScore(Long enterpriseId);

    /**
     * 获取最新评分
     * @param enterpriseId 企业ID
     * @return 评分VO
     */
    ScoreVO getLatestScore(Long enterpriseId);

    /**
     * 获取评分趋势
     * @param enterpriseId 企业ID
     * @param months 查询月数
     * @return 评分趋势
     */
    ScoreTrendVO getScoreTrend(Long enterpriseId, int months);

}
