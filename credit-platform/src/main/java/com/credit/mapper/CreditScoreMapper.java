package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.entity.CreditScore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 信用评分Mapper接口
 */
@Mapper
public interface CreditScoreMapper extends BaseMapper<CreditScore> {

}
