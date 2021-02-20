package com.zhi.cluster.impl;

import com.zhi.cluster.ClusterStrategy;
import com.zhi.remoting.model.ProviderService;
import com.zhi.utils.ip.IPUtil;

import java.util.List;

/**
 * @Description 软负载哈希算法实现
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:04
 */
public class HashClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //获取调用方ip
        String localIP = IPUtil.localIp();
        //获取源地址对应的hashcode
        int hashCode = localIP.hashCode();
        //获取服务列表大小
        int size = providerServices.size();

        return providerServices.get(hashCode % size);
    }
}
