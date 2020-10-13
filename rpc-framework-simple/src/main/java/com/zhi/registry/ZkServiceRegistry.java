package com.zhi.registry;

import com.zhi.utils.zk.CuratorHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @Description 基于 zookeeper 实现服务注册中心
 * @Author WenZhiLuo
 * @Date 2020-10-12 22:33
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    private final CuratorFramework zkClient;
    public ZkServiceRegistry() {
        zkClient = CuratorHelper.getZkClient();
        zkClient.start();
    }
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);
        //服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorHelper.createEphemeralNode(zkClient, servicePath.toString());
        log.info("节点创建成功，节点为:{}", servicePath);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        String serviceAddress = CuratorHelper.getChildrenNodes(zkClient, serviceName).get(0);
        log.info("成功找到服务地址:{}", serviceAddress);
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}