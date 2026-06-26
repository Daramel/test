package com.credit.pay;

/**
 * 支付渠道枚举
 */
public enum PayChannel {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    BALANCE(3, "余额支付"),
    BANK(4, "银行转账");

    private final Integer code;
    private final String description;

    PayChannel(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayChannel fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayChannel channel : values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        return null;
    }

}
