package com.zhi.registry;

import com.zhi.loadbalance.LoadBalance;
import com.zhi.loadbalance.RandomLoadBalance;
import com.zhi.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Description 基于 zookeeper 实现服务发现
 * @Author WenZhiLuo
 * @Date 2020-10-13 15:32
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        List<String> serviceAddressList = CuratorUtils.getChildrenNodes(serviceName);
        //通过负载均衡
        String targetServiceURL = loadBalance.selectServiceAddress(serviceAddressList);
        log.info("成功找到服务地址：[{}]", targetServiceURL);
        String[] socketAddressArray = targetServiceURL.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}