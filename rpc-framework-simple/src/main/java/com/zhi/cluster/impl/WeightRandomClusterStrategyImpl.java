package com.zhi.cluster.impl;

import com.google.common.collect.Lists;
import com.zhi.cluster.ClusterStrategy;
import com.zhi.remoting.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * @Description 软负载加权随机算法实现
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:15
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //存放加权后的服务提供者列表
        List<ProviderService> providerList = Lists.newArrayList();
        for (ProviderService provider : providerServices) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {
                providerList.add(provider.copy());
            }
        }
        int maxLen = providerList.size();
        int index = RandomUtils.nextInt(0, maxLen - 1);
        return providerList.get(index);
    }
}