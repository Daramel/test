package com.credit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.dto.PayResult;
import com.credit.entity.OrderInfo;
import com.credit.entity.PaymentRecord;
import com.credit.mapper.OrderInfoMapper;
import com.credit.mapper.PaymentRecordMapper;
import com.credit.pay.PayChannel;
import com.credit.pay.PayStrategy;
import com.credit.pay.PayStrategyFactory;
import com.credit.service.BalanceService;
import com.credit.service.OrderService;
import com.credit.service.PaymentService;
import com.credit.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private PayStrategyFactory payStrategyFactory;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    @Override
    @Transactional
    public PayResult initiatePay(Long orderId, Integer payMethod) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getPayStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_PAID);
        }
        
        order.setPayMethod(payMethod);
        orderInfoMapper.updateById(order);
        
        if (payMethod.equals(PayChannel.BALANCE.getCode())) {
            boolean success = balancePay(orderId, order.getEnterpriseId());
            PayResult result = new PayResult();
            result.setSuccess(success);
            result.setOrderNo(order.getOrderNo());
            result.setPayAmount(order.getPayAmount());
            if (!success) {
                result.setErrorCode("BALANCE_INSUFFICIENT");
                result.setErrorMsg("余额不足");
            }
            return result;
        }
        
        PayStrategy strategy = payStrategyFactory.getStrategy(payMethod);
        PayResult payResult = strategy.createPayOrder(order);
        
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setOrderNo(order.getOrderNo());
        record.setPayMethod(payMethod);
        record.setPayAmount(order.getPayAmount());
        record.setPayStatus(0);
        paymentRecordMapper.insert(record);
        
        return payResult;
    }

    @Override
    public String createWechatPayOrder(OrderInfo order) {
        PayStrategy strategy = payStrategyFactory.getStrategy(PayChannel.WECHAT.getCode());
        PayResult result = strategy.createPayOrder(order);
        return result.getPrepayId();
    }

    @Override
    public String createAlipayOrder(OrderInfo order) {
        PayStrategy strategy = payStrategyFactory.getStrategy(PayChannel.ALIPAY.getCode());
        PayResult result = strategy.createPayOrder(order);
        return result.getPrepayId();
    }

    @Override
    @Transactional
    public boolean balancePay(Long orderId, Long enterpriseId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        boolean success = balanceService.deductBalance(
                enterpriseId, 
                order.getPayAmount(), 
                "order:" + order.getOrderNo()
        );
        
        if (success) {
            orderService.handlePaySuccess(order.getOrderNo(), "BALANCE_" + order.getOrderNo());
        }
        
        return success;
    }

    @Override
    @Transactional
    public void handleWechatCallback(Map<String, String> params) {
        log.info("微信支付回调参数: {}", params);
        
        PayStrategy strategy = payStrategyFactory.getStrategy(PayChannel.WECHAT.getCode());
        boolean valid = strategy.handleCallback(params);
        
        if (valid) {
            String orderNo = params.get("out_trade_no");
            String transactionId = params.get("transaction_id");
            orderService.handlePaySuccess(orderNo, transactionId);
        }
    }

    @Override
    @Transactional
    public void handleAlipayCallback(Map<String, String> params) {
        log.info("支付宝回调参数: {}", params);
        
        PayStrategy strategy = payStrategyFactory.getStrategy(PayChannel.ALIPAY.getCode());
        boolean valid = strategy.handleCallback(params);
        
        if (valid) {
            String orderNo = params.get("out_trade_no");
            String transactionId = params.get("trade_no");
            orderService.handlePaySuccess(orderNo, transactionId);
        }
    }

    @Override
    public Integer queryPayStatus(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        return order.getPayStatus();
    }

}
