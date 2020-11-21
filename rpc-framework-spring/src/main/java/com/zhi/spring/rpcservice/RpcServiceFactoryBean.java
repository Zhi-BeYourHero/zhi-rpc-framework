package com.zhi.spring.rpcservice;

import com.zhi.ClientProxy;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Description
 * FactoryBean是一个工厂Bean，可以生成某一个类型Bean实例，它最大的一个作用是：可以让我们自定义Bean的创建过程。
 * @Author WenZhiLuo
 * @Date 2020-10-27 10:05
 */
public class RpcServiceFactoryBean<T> implements FactoryBean<T> {
    private Class<T> rpcServiceInterface;
    public RpcServiceFactoryBean() {
    }
    public RpcServiceFactoryBean(Class<T> rpcServiceInterface) {
        this.rpcServiceInterface = rpcServiceInterface;
    }
    @Override
    public T getObject() throws Exception {
        if (rpcServiceInterface == null) {
            throw new IllegalStateException();
        }
        return ClientProxy.getServiceProxy(rpcServiceInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcServiceInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}