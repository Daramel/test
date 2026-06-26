package com.credit.pay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付策略工厂
 */
@Component
public class PayStrategyFactory {

    private final Map<Integer, PayStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public PayStrategyFactory(Map<String, PayStrategy> strategies) {
        for (PayStrategy strategy : strategies.values()) {
            strategyMap.put(strategy.getChannel().getCode(), strategy);
        }
    }

    /**
     * 获取支付策略
     * @param payMethod 支付方式
     * @return 支付策略
     */
    public PayStrategy getStrategy(Integer payMethod) {
        PayStrategy strategy = strategyMap.get(payMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + payMethod);
        }
        return strategy;
    }

    /**
     * 判断是否支持该支付方式
     * @param payMethod 支付方式
     * @return 是否支持
     */
    public boolean isSupported(Integer payMethod) {
        return strategyMap.containsKey(payMethod);
    }

}
