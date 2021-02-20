package com.zhi.cluster.impl;

import com.zhi.cluster.ClusterStrategy;
import com.zhi.remoting.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;
import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:12
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int maxLen = providerServices.size();
        int index = RandomUtils.nextInt(0, maxLen - 1);
        return providerServices.get(index);
    }
}