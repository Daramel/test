package com.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.dto.ScoreTrendVO;
import com.credit.dto.ScoreVO;
import com.credit.entity.CreditScore;
import com.credit.entity.SysEnterprise;
import com.credit.mapper.CreditScoreMapper;
import com.credit.mapper.SysEnterpriseMapper;
import com.credit.service.CreditScoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 信用评分服务实现
 */
@Service
public class CreditScoreServiceImpl implements CreditScoreService {

    @Autowired
    private CreditScoreMapper creditScoreMapper;

    @Autowired
    private SysEnterpriseMapper sysEnterpriseMapper;

    private static final String MODEL_VERSION = "v1.0";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScoreVO calculateScore(Long enterpriseId) {
        SysEnterprise enterprise = sysEnterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "企业不存在");
        }

        // 基础分：500分
        int basicScore = 500;
        int operationScore = 0;
        int financeScore = 0;
        int creditRecordScore = 0;
        int potentialScore = 0;

        // 1. 根据企业成立年限加分（每年+5分，上限50分）
        if (enterprise.getEstablishedDate() != null) {
            long years = ChronoUnit.YEARS.between(enterprise.getEstablishedDate(), LocalDate.now());
            operationScore = (int) Math.min(years * 5, 50);
        }

        // 2. 根据注册资本加分（每100万+2分，上限50分）
        if (enterprise.getRegisteredCapital() != null) {
            double capitalMillions = enterprise.getRegisteredCapital().doubleValue() / 1000000;
            financeScore = (int) Math.min(capitalMillions * 2, 50);
        }

        // 3. 根据经营状态加分（存续+30分，在业+50分，其他+0分）
        if (enterprise.getStatus() != null) {
            switch (enterprise.getStatus()) {
                case 0: // 存续
                    creditRecordScore = 30;
                    break;
                case 1: // 在业
                    creditRecordScore = 50;
                    break;
                default: // 其他
                    creditRecordScore = 0;
            }
        }

        // 4. 随机风险扣分（-20到0分）
        Random random = new Random();
        int riskDeduction = random.nextInt(21);

        // 计算总分
        int totalScore = basicScore + operationScore + financeScore + creditRecordScore + creditRecordScore - riskDeduction;

        // 限制在300-900分之间
        totalScore = Math.max(300, Math.min(900, totalScore));

        // 信用等级
        String creditRating = getCreditRating(totalScore);

        // 保存评分记录
        CreditScore score = new CreditScore();
        score.setEnterpriseId(enterpriseId);
        score.setTotalScore(totalScore);
        score.setCreditRating(creditRating);
        score.setModelVersion(MODEL_VERSION);
        score.setBasicScore(basicScore);
        score.setOperationScore(operationScore);
        score.setFinanceScore(financeScore);
        score.setCreditRecordScore(creditRecordScore);
        score.setPotentialScore(potentialScore);
        score.setEvaluationTime(LocalDateTime.now());
        score.setExpireTime(LocalDateTime.now().plusMonths(12));
        creditScoreMapper.insert(score);

        // 构建返回VO
        return buildScoreVO(score, enterprise);
    }

    @Override
    public ScoreVO getLatestScore(Long enterpriseId) {
        LambdaQueryWrapper<CreditScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditScore::getEnterpriseId, enterpriseId)
                .orderByDesc(CreditScore::getEvaluationTime)
                .last("LIMIT 1");

        CreditScore score = creditScoreMapper.selectOne(wrapper);
        if (score == null) {
            return null;
        }

        SysEnterprise enterprise = sysEnterpriseMapper.selectById(enterpriseId);
        return buildScoreVO(score, enterprise);
    }

    @Override
    public ScoreTrendVO getScoreTrend(Long enterpriseId, int months) {
        SysEnterprise enterprise = sysEnterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "企业不存在");
        }

        LocalDateTime startTime = LocalDateTime.now().minusMonths(months);
        LambdaQueryWrapper<CreditScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditScore::getEnterpriseId, enterpriseId)
                .ge(CreditScore::getEvaluationTime, startTime)
                .orderByAsc(CreditScore::getEvaluationTime);

        List<CreditScore> scores = creditScoreMapper.selectList(wrapper);

        ScoreTrendVO vo = new ScoreTrendVO();
        vo.setEnterpriseId(enterpriseId);
        vo.setEnterpriseName(enterprise.getName());

        List<ScoreTrendVO.ScoreTrendItem> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Integer previousScore = null;
        for (CreditScore score : scores) {
            ScoreTrendVO.ScoreTrendItem item = new ScoreTrendVO.ScoreTrendItem();
            item.setEvaluationTime(score.getEvaluationTime().format(formatter));
            item.setTotalScore(score.getTotalScore());
            item.setCreditRating(score.getCreditRating());
            trendList.add(item);

            if (previousScore != null) {
                vo.setChange(score.getTotalScore() - previousScore);
            }
            previousScore = score.getTotalScore();
        }

        vo.setTrendList(trendList);

        if (scores.size() >= 2) {
            int lastScore = scores.get(scores.size() - 1).getTotalScore();
            int firstScore = scores.get(0).getTotalScore();
            int change = lastScore - firstScore;
            vo.setChange(change);
            if (change > 0) {
                vo.setTrend("UP");
            } else if (change < 0) {
                vo.setTrend("DOWN");
            } else {
                vo.setTrend("STABLE");
            }
        } else {
            vo.setChange(0);
            vo.setTrend("STABLE");
        }

        return vo;
    }

    private ScoreVO buildScoreVO(CreditScore score, SysEnterprise enterprise) {
        ScoreVO vo = new ScoreVO();
        BeanUtils.copyProperties(score, vo);
        if (enterprise != null) {
            vo.setEnterpriseName(enterprise.getName());
        }

        // 判断是否有效（过期时间未到）
        vo.setIsValid(score.getExpireTime().isAfter(LocalDateTime.now()));
        return vo;
    }

    private String getCreditRating(int score) {
        if (score >= 800) {
            return "AAA";
        } else if (score >= 700) {
            return "AA";
        } else if (score >= 600) {
            return "A";
        } else if (score >= 500) {
            return "BBB";
        } else if (score >= 400) {
            return "BB";
        } else {
            return "B";
        }
    }

}
