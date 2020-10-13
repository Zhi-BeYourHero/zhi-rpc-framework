package com.zhi.registry;

import java.net.InetSocketAddress;

/**
 * @Description 服务注册接口
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:20
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}