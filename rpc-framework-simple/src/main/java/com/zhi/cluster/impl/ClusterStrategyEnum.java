package com.zhi.cluster.impl;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:03
 */
public enum ClusterStrategyEnum {
    //随机算法
    RANDOM("Random"),
    //权重随机算法
    WEIGHT_RANDOM("WeightRandom"),
    //轮询算法
    POLLING("Polling"),
    //权重轮询算法
    WEIGHT_POLLING("WeightPolling"),
    //源地址hash算法
    HASH("Hash"),
    //一致性哈希算法
    CONSISTENT_HASH("ConsistentHash"),
    ;

    ClusterStrategyEnum(String code) {
        this.code = code;
    }

    public static ClusterStrategyEnum queryByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ClusterStrategyEnum strategy : values()) {
            if (StringUtils.equals(code, strategy.getCode())) {
                return strategy;
            }
        }
        return null;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
