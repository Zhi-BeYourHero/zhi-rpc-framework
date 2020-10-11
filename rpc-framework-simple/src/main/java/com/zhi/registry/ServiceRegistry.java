package com.zhi.registry;

/**
 * @Description 服务注册中心接口
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:20
 */
public interface ServiceRegistry {
    <T> void register(T service);
    Object getService(String serviceName);
}