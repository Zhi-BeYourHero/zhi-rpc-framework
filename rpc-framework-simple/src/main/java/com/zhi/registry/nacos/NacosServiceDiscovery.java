package com.zhi.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhi.enums.RpcErrorMessageEnum;
import com.zhi.exception.RpcException;
import com.zhi.loadbalance.LoadBalance;
import com.zhi.loadbalance.RandomLoadBalance;
import com.zhi.registry.ServiceDiscovery;
import com.zhi.registry.nacos.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-01-25 9:49
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public NacosServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }
    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(rpcServiceName);
            if (instances.size() == 0) {
                log.error("找不到对应的服务: " + rpcServiceName);
                throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
            }
            Instance instance = loadBalance.selectServiceAddress(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
    }
}