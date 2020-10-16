package com.zhi.registry;

import com.zhi.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description 实现服务注册
 * @Author WenZhiLuo
 * @Date 2020-10-12 22:33
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorUtils.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);
        //服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorUtils.createPersistentNode(servicePath.toString());
        log.info("节点创建成功，节点为:{}", servicePath);
    }
}