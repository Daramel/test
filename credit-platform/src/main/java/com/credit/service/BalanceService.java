package com.credit.service;

import com.credit.common.PageResult;
import com.credit.dto.BalanceVO;
import com.credit.entity.BalanceLog;

import java.math.BigDecimal;

/**
 * 余额服务接口
 */
public interface BalanceService {

    /**
     * 获取余额
     * @param enterpriseId 企业ID
     * @return 余额信息
     */
    BalanceVO getBalance(Long enterpriseId);

    /**
     * 余额充值
     * @param enterpriseId 企业ID
     * @param amount 充值金额
     * @param payMethod 支付方式
     */
    void recharge(Long enterpriseId, BigDecimal amount, String payMethod);

    /**
     * 扣减余额
     * @param enterpriseId 企业ID
     * @param amount 扣减金额
     * @param reason 扣减原因
     * @return 是否成功
     */
    boolean deductBalance(Long enterpriseId, BigDecimal amount, String reason);

    /**
     * 余额退款
     * @param enterpriseId 企业ID
     * @param amount 退款金额
     * @param orderId 订单ID
     */
    void refund(Long enterpriseId, BigDecimal amount, Long orderId);

    /**
     * 获取余额流水
     * @param enterpriseId 企业ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<BalanceLog> getBalanceLog(Long enterpriseId, Integer pageNum, Integer pageSize);

    /**
     * 增加余额(内部使用)
     * @param enterpriseId 企业ID
     * @param amount 增加金额
     * @param changeType 变动类型
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     * @param remark 备注
     */
    void addBalance(Long enterpriseId, BigDecimal amount, Integer changeType,
                    String relatedType, String relatedId, String remark);

}
