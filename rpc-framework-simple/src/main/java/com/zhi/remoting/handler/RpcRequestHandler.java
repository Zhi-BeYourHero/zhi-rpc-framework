package com.zhi.remoting.handler;

import com.zhi.registry.IRegisterCenter4Provider;
import com.zhi.registry.zk.util.RegisterCenter;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.exception.RpcException;
import com.zhi.remoting.model.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @Description RpcRequest处理类
 * 优化①：原来的Service是作为handle方法的参数传进来的，在NettyServerHandler里通过注册中心获取的，
 * 现在移到Handler来了...
 * 优化②：exception在私有方法进行处理而不再handle方法进行处理...
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:40
 */
@Slf4j
public class RpcRequestHandler {
    /**
     * 服务端限流
     */
    private static final Map<String, Semaphore> SERVICE_KEY_SEMAPHORE_MAP = new ConcurrentHashMap<>();
    /**
     * 处理rpcRequest：调用对应的方法，并返回方法执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        return invokeTargetMethod(rpcRequest);
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     * @param rpcRequest 客户端请求
     * @return 目标方法执行的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest) {
        Object result = null;
        try {
            //从服务调用对象里获取服务提供者信息
            ProviderService metaDataModel = rpcRequest.getProviderService();
            long consumeTimeOut = rpcRequest.getInvokeTimeout();
            final String methodName = rpcRequest.getMethodName();

            //根据方法名称定位到具体某一个服务提供者
            String serviceKey = metaDataModel.getServiceItf().getName();
            //获取限流工具类
            int workerThread = metaDataModel.getWorkerThreads();
            Semaphore semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
            if (semaphore == null) {
                synchronized (SERVICE_KEY_SEMAPHORE_MAP) {
                    semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
                    if (semaphore == null) {
                        semaphore = new Semaphore(workerThread);
                        SERVICE_KEY_SEMAPHORE_MAP.put(serviceKey, semaphore);
                    }
                }
            }
            /**
             * !!!特别注意：
             * 一次远程服务调用重要步骤：
             * 当服务生产者机器在部署应用的时候，spring扫描xml生成bean工厂时，会解析自定义的服务信息（接口、实现类、集群地址等）。
             * 接着这个bean被注册到context环境中、第一次从bean工厂doGetBean依赖注入时候，这个bean会向Zookeeper集群注册自身的服务信息。
             * 此时Zookeeper的某个临时结点中就有了这个生产者发布的服务信息。
             * 而后这个服务生产者实例化了Netty服务端，并将它绑定在本机的IP地址和端口号上持续监听。
             * 每当有某个客户端通过netty的client、使用IP地址+端口号调用到这个生产者的netty服务端时——
             * 服务生产者的netty服务端就会先解码、获取客户端想要调用的服务、使用反射去执行目标方法、将产生的结果写入通道中，客户端就会收到服务端执行的结果。
             */
            // 获取注册中心服务：从单例Zookeeper包装类`RegisterCenter`注册中心中根据`serviceKey`拿到这个服务方提供的所有方法
            IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
            List<ProviderService> localProviderCaches = registerCenter4Provider.getProviderServiceMap().get(serviceKey);
            // 服务调用结果
            // 限流令牌，true代表获得
            boolean acquire;
            // 找到要调用的目标服务
            ProviderService localProviderCache = localProviderCaches.stream()
                    .filter(providerService -> StringUtils.equals(providerService.getServiceMethod().getName(), methodName)).iterator().next();
            Object service = localProviderCache.getServiceObject();
            // !!!最重要的核心是在服务提供方这里使用反射调用服务。
            //利用反射发起服务调用
            Method method = localProviderCache.getServiceMethod();
            //利用semaphore实现限流(能获得调用权利才可以调用)
            acquire = semaphore.tryAcquire(consumeTimeOut, TimeUnit.MILLISECONDS);
            if (acquire) {
                result = method.invoke(service, rpcRequest.getParameters());
            }
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}