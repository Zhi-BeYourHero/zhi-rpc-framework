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
 * 默认的服务注册中心实现，通过 Map 保存服务信息，可以通过 zookeeper 来改进
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:16
 */
public class DefaultServiceRegistry implements ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceRegistry.class);

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
     * @param service
     * @param <T>
     */
    @Override
    public <T> void register(T service) {
        //Canonical：经典的，权威的
        String serviceName = service.getClass().getCanonicalName();
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }
        REGISTERED_SERVICE.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> anInterface : interfaces) {
            SERVICE_MAP.put(anInterface.getCanonicalName(), service);
        }
        LOGGER.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    /**
     * @param serviceName 这里的服务名其实就是接口名
     * @return
     */
    @Override
    public Object getService(String serviceName) {
        Object service = SERVICE_MAP.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}