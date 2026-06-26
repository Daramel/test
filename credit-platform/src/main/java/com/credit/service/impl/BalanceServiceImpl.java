package com.credit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.common.PageResult;
import com.credit.dto.BalanceVO;
import com.credit.entity.BalanceLog;
import com.credit.entity.SysEnterprise;
import com.credit.mapper.BalanceLogMapper;
import com.credit.mapper.SysEnterpriseMapper;
import com.credit.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 余额服务实现类
 */
@Service
public class BalanceServiceImpl implements BalanceService {

    /**
     * 模拟企业余额存储(实际应存在数据库中)
     */
    private static final Map<Long, BigDecimal> BALANCE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, BigDecimal> FROZEN_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private SysEnterpriseMapper sysEnterpriseMapper;

    @Autowired
    private BalanceLogMapper balanceLogMapper;

    @Override
    public BalanceVO getBalance(Long enterpriseId) {
        BalanceVO vo = new BalanceVO();
        vo.setEnterpriseId(enterpriseId);
        vo.setBalance(getBalanceFromCache(enterpriseId));
        vo.setFrozenAmount(getFrozenFromCache(enterpriseId));
        vo.setAvailableBalance(vo.getBalance().subtract(vo.getFrozenAmount()));
        return vo;
    }

    @Override
    @Transactional
    public void recharge(Long enterpriseId, BigDecimal amount, String payMethod) {
        addBalance(enterpriseId, amount, 1, "recharge", null, "充值:" + payMethod);
    }

    @Override
    @Transactional
    public boolean deductBalance(Long enterpriseId, BigDecimal amount, String reason) {
        BigDecimal balance = getBalanceFromCache(enterpriseId);
        BigDecimal frozen = getFrozenFromCache(enterpriseId);
        BigDecimal available = balance.subtract(frozen);
        
        if (available.compareTo(amount) < 0) {
            return false;
        }
        
        BigDecimal newBalance = balance.subtract(amount);
        BALANCE_CACHE.put(enterpriseId, newBalance);
        
        BalanceLog log = new BalanceLog();
        log.setEnterpriseId(enterpriseId);
        log.setChangeType(2);
        log.setAmount(amount.negate());
        log.setBalanceAfter(newBalance);
        log.setRelatedType("order");
        log.setRelatedId(reason);
        log.setRemark("消费:" + reason);
        balanceLogMapper.insert(log);
        
        return true;
    }

    @Override
    @Transactional
    public void refund(Long enterpriseId, BigDecimal amount, Long orderId) {
        addBalance(enterpriseId, amount, 3, "refund", String.valueOf(orderId), "退款:订单" + orderId);
    }

    @Override
    public PageResult<BalanceLog> getBalanceLog(Long enterpriseId, Integer pageNum, Integer pageSize) {
        Page<BalanceLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BalanceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BalanceLog::getEnterpriseId, enterpriseId);
        wrapper.orderByDesc(BalanceLog::getCreateTime);
        
        IPage<BalanceLog> pageResult = balanceLogMapper.selectPage(page, wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getRecords(), pageNum.longValue(), pageSize.longValue());
    }

    @Override
    @Transactional
    public void addBalance(Long enterpriseId, BigDecimal amount, Integer changeType,
                           String relatedType, String relatedId, String remark) {
        BigDecimal oldBalance = getBalanceFromCache(enterpriseId);
        BigDecimal newBalance = oldBalance.add(amount);
        BALANCE_CACHE.put(enterpriseId, newBalance);
        
        BalanceLog log = new BalanceLog();
        log.setEnterpriseId(enterpriseId);
        log.setChangeType(changeType);
        log.setAmount(amount);
        log.setBalanceAfter(newBalance);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        log.setRemark(remark);
        balanceLogMapper.insert(log);
    }

    private BigDecimal getBalanceFromCache(Long enterpriseId) {
        return BALANCE_CACHE.computeIfAbsent(enterpriseId, k -> BigDecimal.ZERO);
    }

    private BigDecimal getFrozenFromCache(Long enterpriseId) {
        return FROZEN_CACHE.computeIfAbsent(enterpriseId, k -> BigDecimal.ZERO);
    }

}
