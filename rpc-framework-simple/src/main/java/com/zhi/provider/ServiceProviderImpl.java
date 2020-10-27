package com.zhi.provider;

import com.zhi.enumeration.RpcErrorMessage;
import com.zhi.exception.RpcException;
import com.zhi.registry.ServiceRegistry;
import com.zhi.registry.zk.ZkServiceRegistry;
import com.zhi.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了 ServiceProvider 接口，可以将其看做是一个保存和提供服务实例对象的实例
 *
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:16
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况（通过 group 分组）
     * key:service/interface name
     * value:service
     * v[2.0]从原来的只有final修改为static final
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();
    //这里的是serviceMap的keySet
    private static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();
    private final ServiceRegistry serviceRegistry = new ZkServiceRegistry();

    /**
     * TODO 修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去 -> 注册服务名与服务对象
     * @param service
     */
    @Override
    public void addServiceProvider(Object service, Class<?> serviceClass) {
        //Canonical：经典的，权威的
        String serviceName = serviceClass.getCanonicalName();
        log.info("serviceProvider.getClass().getCanonicalName():{}", serviceName);
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }
        REGISTERED_SERVICE.add(serviceName);
        SERVICE_MAP.put(serviceName, service);
        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = SERVICE_MAP.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            this.addServiceProvider(service, anInterface);
            serviceRegistry.registerService(anInterface.getCanonicalName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}