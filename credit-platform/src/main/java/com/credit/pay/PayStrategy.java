package com.credit.pay;

import com.credit.dto.PayResult;
import com.credit.entity.OrderInfo;

import java.util.Map;

/**
 * 支付策略接口
 */
public interface PayStrategy {

    /**
     * 创建支付订单
     * @param order 订单信息
     * @return 支付结果
     */
    PayResult createPayOrder(OrderInfo order);

    /**
     * 处理支付回调
     * @param params 回调参数
     * @return 是否成功
     */
    boolean handleCallback(Map<String, String> params);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态: 0-待支付, 1-已支付, 2-支付失败
     */
    Integer queryPayStatus(String orderNo);

    /**
     * 获取支付渠道
     * @return 支付渠道
     */
    PayChannel getChannel();

}
