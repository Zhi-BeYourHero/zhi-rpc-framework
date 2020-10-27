package com.zhi.registry;

import java.net.InetSocketAddress;

/**
 * @Description 服务发现接口
 * @Author WenZhiLuo
 * @Date 2020-10-13 15:30
 */
public interface ServiceDiscovery {
    /**
     * 查找服务
     *
     * @param rpcServiceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String rpcServiceName);
}