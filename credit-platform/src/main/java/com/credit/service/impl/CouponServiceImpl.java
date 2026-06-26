package com.credit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.credit.common.BusinessException;
import com.credit.common.ErrorCode;
import com.credit.dto.CouponListVO;
import com.credit.entity.Coupon;
import com.credit.entity.UserCoupon;
import com.credit.mapper.CouponMapper;
import com.credit.mapper.UserCouponMapper;
import com.credit.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券服务实现类
 */
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    @Transactional
    public void grantCoupon(Long enterpriseId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);
        }
        if (coupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
        
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setEnterpriseId(enterpriseId);
        userCoupon.setCouponId(couponId);
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setStatus(0);
        userCoupon.setExpireTime(LocalDateTime.now().plusDays(coupon.getValidDays()));
        userCouponMapper.insert(userCoupon);
        
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponMapper.updateById(coupon);
    }

    @Override
    public List<CouponListVO> getUserCoupons(Long enterpriseId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getEnterpriseId, enterpriseId);
        wrapper.orderByDesc(UserCoupon::getReceiveTime);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        
        List<CouponListVO> result = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            CouponListVO vo = new CouponListVO();
            vo.setId(uc.getId());
            vo.setCouponId(uc.getCouponId());
            vo.setReceiveTime(uc.getReceiveTime());
            vo.setExpireTime(uc.getExpireTime());
            vo.setStatus(uc.getStatus());
            
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon != null) {
                vo.setCouponName(coupon.getCouponName());
                vo.setCouponType(coupon.getCouponType());
                vo.setDenomination(coupon.getDenomination());
                vo.setThresholdAmount(coupon.getThresholdAmount());
            }
            
            if (uc.getStatus() == 0 && uc.getExpireTime().isBefore(LocalDateTime.now())) {
                vo.setStatus(2);
            }
            
            result.add(vo);
        }
        
        return result;
    }

    @Override
    @Transactional
    public CouponUseResult useCoupon(Long enterpriseId, Long couponId, Long orderId) {
        CouponUseResult result = new CouponUseResult();
        
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getEnterpriseId, enterpriseId)
               .eq(UserCoupon::getCouponId, couponId)
               .eq(UserCoupon::getStatus, 0);
        UserCoupon userCoupon = userCouponMapper.selectOne(wrapper);
        
        if (userCoupon == null) {
            result.setSuccess(false);
            result.setErrorMessage("优惠券不存在或已使用");
            return result;
        }
        
        if (userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            userCoupon.setStatus(2);
            userCouponMapper.updateById(userCoupon);
            result.setSuccess(false);
            result.setErrorMessage("优惠券已过期");
            return result;
        }
        
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            result.setSuccess(false);
            result.setErrorMessage("优惠券不存在");
            return result;
        }
        
        userCoupon.setOrderId(orderId);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setStatus(1);
        userCouponMapper.updateById(userCoupon);
        
        result.setSuccess(true);
        result.setDiscountAmount(coupon.getDenomination());
        return result;
    }

    @Override
    public BigDecimal calculateDiscount(Long enterpriseId, BigDecimal orderAmount) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getEnterpriseId, enterpriseId)
               .eq(UserCoupon::getStatus, 0)
               .gt(UserCoupon::getExpireTime, LocalDateTime.now());
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        
        BigDecimal maxDiscount = BigDecimal.ZERO;
        
        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon == null) {
                continue;
            }
            
            if (coupon.getThresholdAmount() != null && 
                orderAmount.compareTo(coupon.getThresholdAmount()) < 0) {
                continue;
            }
            
            BigDecimal discount = BigDecimal.ZERO;
            switch (coupon.getCouponType()) {
                case 1:
                case 3:
                    discount = coupon.getDenomination();
                    break;
                case 2:
                    discount = orderAmount.multiply(BigDecimal.ONE.subtract(
                            coupon.getDenomination().divide(BigDecimal.valueOf(100))));
                    break;
            }
            
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
            }
        }
        
        return maxDiscount;
    }

    @Override
    public Coupon getCouponById(Long couponId) {
        return couponMapper.selectById(couponId);
    }

}
