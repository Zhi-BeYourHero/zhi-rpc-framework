package com.zhi.registry.zk;

import com.zhi.loadbalance.LoadBalance;
import com.zhi.loadbalance.RandomLoadBalance;
import com.zhi.registry.ServiceDiscovery;
import com.zhi.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

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
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceAddressList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
        //通过负载均衡
        String targetServiceURL = loadBalance.selectServiceAddress(serviceAddressList);
        log.info("成功找到服务地址：[{}]", targetServiceURL);
        String[] socketAddressArray = targetServiceURL.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}