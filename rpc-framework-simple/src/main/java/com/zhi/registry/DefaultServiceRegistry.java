package com.zhi.registry;

import com.zhi.enumeration.RpcErrorMessageEnum;
import com.zhi.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 默认的服务注册表
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:16
 */
public class DefaultServiceRegistry implements ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况
     * key:service/interface name
     * value:service
     */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    //这里的是serviceMap的keySet
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * TODO 修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去
     * @param service
     * @param <T>
     */
    @Override
    public <T> void register(T service) {
        //Canonical：经典的，权威的
        String serviceName = service.getClass().getCanonicalName();
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> anInterface : interfaces) {
            serviceMap.put(anInterface.getCanonicalName(), service);
        }
        LOGGER.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    /**
     * @param serviceName 这里的服务名其实就是接口名
     * @return
     */
    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}