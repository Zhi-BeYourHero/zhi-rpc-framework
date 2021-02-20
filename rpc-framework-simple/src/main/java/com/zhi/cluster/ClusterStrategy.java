package com.zhi.cluster;

import com.zhi.remoting.model.ProviderService;

import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:01
 */
public interface ClusterStrategy {
    /**
     * 负载策略算法
     *
     * @param providerServices
     * @return
     */
    ProviderService select(List<ProviderService> providerServices);
}