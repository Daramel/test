package com.credit.pay;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.credit.dto.PayResult;
import com.credit.entity.OrderInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信支付策略实现(模拟实现)
 */
@Component
public class WechatPayStrategy implements PayStrategy {

    @Override
    public PayResult createPayOrder(OrderInfo order) {
        PayResult result = new PayResult();
        result.setSuccess(true);
        result.setOrderNo(order.getOrderNo());
        result.setPayAmount(order.getPayAmount());
        // 模拟生成预支付订单号
        result.setPrepayId("WX" + IdUtil.getSnowflakeNextId());
        // 模拟二维码内容(实际应该是微信支付统一下单接口返回)
        result.setPayUrl("weixin://wxpay/bizpayurl?pr=" + result.getPrepayId());
        return result;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        // 模拟验证: 微信支付回调验证签名等
        if (params == null || params.isEmpty()) {
            return false;
        }
        String returnCode = params.get("return_code");
        return "SUCCESS".equals(returnCode);
    }

    @Override
    public Integer queryPayStatus(String orderNo) {
        // 模拟查询: 实际应该调用微信支付查单接口
        // 这里默认返回待支付状态
        return 0;
    }

    @Override
    public PayChannel getChannel() {
        return PayChannel.WECHAT;
    }

}
