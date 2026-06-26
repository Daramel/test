package com.credit.service;

import com.credit.dto.PayResult;
import com.credit.entity.OrderInfo;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 发起支付
     * @param orderId 订单ID
     * @param payMethod 支付方式
     * @return 支付结果
     */
    PayResult initiatePay(Long orderId, Integer payMethod);

    /**
     * 微信支付下单
     * @param order 订单信息
     * @return 预支付订单号
     */
    String createWechatPayOrder(OrderInfo order);

    /**
     * 支付宝支付下单
     * @param order 订单信息
     * @return 预支付订单号
     */
    String createAlipayOrder(OrderInfo order);

    /**
     * 余额支付
     * @param orderId 订单ID
     * @param enterpriseId 企业ID
     * @return 是否成功
     */
    boolean balancePay(Long orderId, Long enterpriseId);

    /**
     * 处理微信支付回调
     * @param params 回调参数
     */
    void handleWechatCallback(java.util.Map<String, String> params);

    /**
     * 处理支付宝回调
     * @param params 回调参数
     */
    void handleAlipayCallback(java.util.Map<String, String> params);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态: 0-待支付, 1-已支付, 2-支付失败
     */
    Integer queryPayStatus(String orderNo);

}
