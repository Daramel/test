package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.entity.BalanceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 余额流水Mapper接口
 */
@Mapper
public interface BalanceLogMapper extends BaseMapper<BalanceLog> {

}
