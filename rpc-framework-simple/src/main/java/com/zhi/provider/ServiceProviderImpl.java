package com.zhi.provider;

import com.zhi.enumeration.RpcErrorMessageEnum;
import com.zhi.exception.RpcException;
import com.zhi.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:16
 */
public class ServiceProviderImpl implements ServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况
     * key:service/interface name
     * value:service
     * v[2.0]从原来的只有final修改为static final
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();
    //这里的是serviceMap的keySet
    private static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();


    /**
     * TODO 修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去
     * @param serviceProvider
     * @param <T>
     */
    @Override
    public <T> void addServiceProvider(T serviceProvider) {
        //Canonical：经典的，权威的
        String serviceName = serviceProvider.getClass().getCanonicalName();
        LOGGER.info("serviceProvider.getClass().getCanonicalName():{}", serviceName);
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }
        REGISTERED_SERVICE.add(serviceName);
        Class<?>[] interfaces = serviceProvider.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> anInterface : interfaces) {
            SERVICE_MAP.put(anInterface.getCanonicalName(), serviceProvider);
        }
        LOGGER.info("Add service: {} and interfaces:{}", serviceName, serviceProvider.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = SERVICE_MAP.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}