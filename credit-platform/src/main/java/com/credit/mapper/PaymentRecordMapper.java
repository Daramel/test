package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

}
