package com.zhi.cluster.engine;

import com.zhi.cluster.ClusterStrategy;
import com.zhi.cluster.impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 负载均衡引擎
 * @Author WenZhiLuo
 * @Date 2021-02-19 17:56
 */
public class ClusterEngine {

    private static final Map<ClusterStrategyEnum, ClusterStrategy> CLUSTER_STRATEGY_MAP = new ConcurrentHashMap<>();

    static {
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.Random, new RandomClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.WeightRandom, new WeightRandomClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.Polling, new PollingClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.WeightPolling, new WeightPollingClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.Hash, new HashClusterStrategyImpl());
    }

    public static ClusterStrategy queryClusterStrategy(String clusterStrategy) {
        ClusterStrategyEnum clusterStrategyEnum = ClusterStrategyEnum.queryByCode(clusterStrategy);
        if (clusterStrategyEnum == null) {
            //默认选择随机算法
            return new RandomClusterStrategyImpl();
        }
        return CLUSTER_STRATEGY_MAP.get(clusterStrategyEnum);
    }
}