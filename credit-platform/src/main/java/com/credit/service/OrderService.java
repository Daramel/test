package com.credit.service;

import com.credit.common.PageResult;
import com.credit.dto.OrderCreateRequest;
import com.credit.dto.OrderListVO;
import com.credit.entity.OrderInfo;

import java.math.BigDecimal;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     * @param enterpriseId 企业ID
     * @param userId 用户ID
     * @param orderType 订单类型
     * @param goodsName 商品名称
     * @param totalAmount 订单金额
     * @param reportType 报告类型
     * @param enterpriseTargetId 目标企业ID
     * @return 订单信息
     */
    OrderInfo createOrder(Long enterpriseId, Long userId, Integer orderType, 
                          String goodsName, BigDecimal totalAmount, 
                          Integer reportType, Long enterpriseTargetId);

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单信息
     */
    OrderInfo getOrderDetail(Long orderId);

    /**
     * 获取订单列表
     * @param enterpriseId 企业ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态筛选
     * @return 分页结果
     */
    PageResult<OrderListVO> getOrderList(Long enterpriseId, Integer pageNum, 
                                         Integer pageSize, Integer status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 处理支付成功回调
     * @param orderNo 订单编号
     * @param transactionId 交易号
     */
    void handlePaySuccess(String orderNo, String transactionId);

    /**
     * 申请退款
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     */
    void applyRefund(Long orderId, Long userId, String reason);

}
