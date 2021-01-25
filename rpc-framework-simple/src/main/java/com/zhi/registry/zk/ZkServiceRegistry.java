package com.zhi.registry.zk;

import com.zhi.registry.ServiceRegistry;
import com.zhi.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @Description 实现服务注册
 * @Author WenZhiLuo
 * @Date 2020-10-12 22:33
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        //服务子节点下注册子节点：服务地址
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
        log.info("节点创建成功，节点为:{}", servicePath);
    }
}