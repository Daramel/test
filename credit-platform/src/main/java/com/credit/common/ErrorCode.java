package com.credit.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    
    // 400xx - 参数错误
    BAD_REQUEST(40001, "请求参数错误"),
    VALIDATION_ERROR(40002, "数据校验失败"),
    
    // 401xx - 认证授权错误
    UNAUTHORIZED(40101, "未登录或登录已过期"),
    TOKEN_EXPIRED(40102, "Token已过期"),
    TOKEN_INVALID(40103, "Token无效"),
    USERNAME_PASSWORD_ERROR(40104, "用户名或密码错误"),
    
    // 403xx - 权限错误
    FORBIDDEN(40301, "没有访问权限"),
    
    // 404xx - 资源错误
    NOT_FOUND(40401, "资源不存在"),
    USER_NOT_FOUND(40402, "用户不存在"),
    ENTERPRISE_NOT_FOUND(40403, "企业不存在"),
    
    // 409xx - 冲突错误
    USERNAME_EXISTS(40901, "用户名已存在"),
    ENTERPRISE_EXISTS(40902, "企业已存在"),
    
    // 500xx - 服务器错误
    INTERNAL_ERROR(50001, "服务器内部错误"),
    DATA_ACCESS_ERROR(50002, "数据访问错误"),
    
    // 业务错误
    BUSINESS_ERROR(60001, "业务处理失败"),
    
    // 订单错误
    ORDER_NOT_FOUND(60010, "订单不存在"),
    ORDER_PAID(60011, "订单已支付"),
    ORDER_CANNOT_CANCEL(60012, "订单无法取消"),
    NO_PERMISSION(60013, "无权限操作"),
    REFUND_NOT_ALLOWED(60014, "不允许退款"),
    
    // 优惠券错误
    COUPON_NOT_FOUND(60020, "优惠券不存在"),
    COUPON_NOT_AVAILABLE(60021, "优惠券不可用"),
    COUPON_OUT_OF_STOCK(60022, "优惠券已领完"),
    COUPON_ALREADY_USED(60023, "优惠券已使用"),
    
    // 余额错误
    BALANCE_NOT_FOUND(60030, "余额账户不存在"),
    BALANCE_INSUFFICIENT(60031, "余额不足");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
