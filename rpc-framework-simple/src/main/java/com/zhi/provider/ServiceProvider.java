package com.zhi.provider;

/**
 * @Description 保存和提供服务实例对象，服务端使用
 * @Author WenZhiLuo
 * @Date 2020-10-12 13:39
 */
public interface ServiceProvider {
    /**
     * 保存服务提供者
     */
    <T> void addServiceProvider(T serviceProvider);

    /**
     * 提供服务提供者
     */
    Object getServiceProvider(String serviceName);
}