package com.credit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.common.PageResult;
import com.credit.dto.OrderListVO;
import com.credit.entity.OrderInfo;
import com.credit.entity.PaymentRecord;
import com.credit.entity.RefundRecord;
import com.credit.mapper.OrderInfoMapper;
import com.credit.mapper.PaymentRecordMapper;
import com.credit.mapper.RefundRecordMapper;
import com.credit.service.OrderService;
import com.credit.service.PaymentService;
import com.credit.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional
    public OrderInfo createOrder(Long enterpriseId, Long userId, Integer orderType,
                                  String goodsName, BigDecimal totalAmount,
                                  Integer reportType, Long enterpriseTargetId) {
        OrderInfo order = new OrderInfo();
        order.setEnterpriseId(enterpriseId);
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setGoodsName(goodsName);
        order.setOrderAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setReportType(reportType);
        order.setEnterpriseTargetId(enterpriseTargetId);
        order.setPayStatus(0);
        order.setOrderStatus(0);
        order.setOrderNo(generateOrderNo());
        
        if (orderType == 1 && reportType != null) {
            if (reportType == 1) {
                order.setProductType(1);
                order.setProductName("基础信用报告");
            } else if (reportType == 2) {
                order.setProductType(2);
                order.setProductName("深度征信报告");
            } else if (reportType == 3) {
                order.setProductType(3);
                order.setProductName("专项报告");
            }
        }
        
        orderInfoMapper.insert(order);
        return order;
    }

    @Override
    public OrderInfo getOrderDetail(Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    public PageResult<OrderListVO> getOrderList(Long enterpriseId, Integer pageNum,
                                                  Integer pageSize, Integer status) {
        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getEnterpriseId, enterpriseId);
        if (status != null) {
            wrapper.eq(OrderInfo::getPayStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        
        IPage<OrderInfo> pageResult = orderInfoMapper.selectPage(page, wrapper);
        
        List<OrderListVO> voList = pageResult.getRecords().stream()
                .map(order -> {
                    OrderListVO vo = new OrderListVO();
                    BeanUtil.copyProperties(order, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(pageResult.getTotal(), voList, pageNum.longValue(), pageSize.longValue());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        if (order.getPayStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        
        order.setPayStatus(2);
        order.setOrderStatus(3);
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional
    public void handlePaySuccess(String orderNo, String transactionId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getPayStatus() != 0) {
            return;
        }
        
        order.setPayStatus(1);
        order.setOrderStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setTransactionId(transactionId);
        orderInfoMapper.updateById(order);
        
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getOrderId());
        record.setOrderNo(orderNo);
        record.setPayMethod(order.getPayMethod());
        record.setPayAmount(order.getPayAmount());
        record.setTransactionId(transactionId);
        record.setPayStatus(1);
        record.setPayTime(LocalDateTime.now());
        paymentRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void applyRefund(Long orderId, Long userId, String reason) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        if (order.getPayStatus() != 1) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED);
        }
        
        RefundRecord refund = new RefundRecord();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setEnterpriseId(order.getEnterpriseId());
        refund.setRefundAmount(order.getPayAmount());
        refund.setRefundReason(reason);
        refund.setRefundType(1);
        refund.setRefundStatus(0);
        refundRecordMapper.insert(refund);
    }

    private String generateOrderNo() {
        return "ORD" + DateUtils.getCurrentDateStr() + System.currentTimeMillis();
    }

    private String generateRefundNo() {
        return "REF" + DateUtils.getCurrentDateStr() + System.currentTimeMillis();
    }

}
