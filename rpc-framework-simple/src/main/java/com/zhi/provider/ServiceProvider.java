package com.zhi.provider;

import com.zhi.entity.RpcServiceProperties;

/**
 * @Description 保存和提供服务实例对象，服务端使用
 * @Author WenZhiLuo
 * @Date 2020-10-12 13:39
 */
public interface ServiceProvider {
    /**
     * 保存服务实例对象和服务实例对象实现的接口类的对应关系
     * @param service      服务实例对象
     * @param serviceClass 服务实例对象实现的接口类
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     * 获取服务实例对象
     * @param rpcServiceProperties service related attributes
     * @return 服务实例对象
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     *  从NettyServer中移到ServiceProvider中
     * @param service 服务实例对象
     */
    void publishService(Object service);

    /**
     * @param service              service object
     * @param rpcServiceProperties service related attributes
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);
}