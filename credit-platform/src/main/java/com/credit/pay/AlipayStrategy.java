package com.credit.pay;

import cn.hutool.core.util.IdUtil;
import com.credit.dto.PayResult;
import com.credit.entity.OrderInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付宝支付策略实现(模拟实现)
 */
@Component
public class AlipayStrategy implements PayStrategy {

    @Override
    public PayResult createPayOrder(OrderInfo order) {
        PayResult result = new PayResult();
        result.setSuccess(true);
        result.setOrderNo(order.getOrderNo());
        result.setPayAmount(order.getPayAmount());
        // 模拟生成预支付订单号
        result.setPrepayId("ALI" + IdUtil.getSnowflakeNextId());
        // 模拟跳转链接(实际应该是支付宝接口返回的表单或链接)
        result.setPayUrl("https://openapi.alipay.com/gateway.do?out_trade_no=" + order.getOrderNo());
        return result;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        // 模拟验证: 支付宝回调验证签名等
        if (params == null || params.isEmpty()) {
            return false;
        }
        String tradeStatus = params.get("trade_status");
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }

    @Override
    public Integer queryPayStatus(String orderNo) {
        // 模拟查询: 实际应该调用支付宝查单接口
        // 这里默认返回待支付状态
        return 0;
    }

    @Override
    public PayChannel getChannel() {
        return PayChannel.ALIPAY;
    }

}
