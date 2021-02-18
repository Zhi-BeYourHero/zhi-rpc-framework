package com.zhi.provider;

import com.zhi.entity.RpcServiceProperties;
import com.zhi.enums.RpcErrorMessageEnum;
import com.zhi.exception.RpcException;
import com.zhi.extension.ExtensionLoader;
import com.zhi.registry.ServiceRegistry;
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
     * key: rpc service name(interface name + version + group)
     * value: service object
     * v[2.0]从原来的只有final修改为static final,然后又改成只有final，且通过无参构造方法进行初始化
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;


    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
//        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("nacos");
    }
    /**
     * TODO 修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去 -> 注册服务名与服务对象
     * @param service
     */
    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
        //Canonical：经典的，权威的
        String rpcServiceName = rpcServiceProperties.getServiceName();
        log.info("serviceProvider.getClass().getCanonicalName():{}", rpcServiceName);
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = serviceMap.get(rpcServiceProperties.getServiceName());
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        System.out.println("发布服务：" + service + rpcServiceProperties);
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
            String serviceName = serviceRelatedInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);
            this.addService(service, serviceRelatedInterface, rpcServiceProperties);
            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}