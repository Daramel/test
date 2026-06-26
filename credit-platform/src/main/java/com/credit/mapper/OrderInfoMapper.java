package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单信息Mapper
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

}
