package com.zhi.revoker;

import com.zhi.cluster.ClusterStrategy;
import com.zhi.cluster.engine.ClusterEngine;
import com.zhi.cluster.failmode.FailMode;
import com.zhi.entity.RpcServiceProperties;
import com.zhi.registry.IRegisterCenter4Invoker;
import com.zhi.registry.zk.util.RegisterCenter;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.model.ProviderService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Description 消费端bean代理工厂，发起一次服务调用，是JDK动态代理的`InvocationHandler`具体实现。
 * @Author WenZhiLuo
 * @Date 2021-02-19 15:17
 */
@Slf4j
public class RevokerProxyBeanFactory  implements InvocationHandler {
    private ExecutorService fixedThreadPool = null;

    //服务接口
    private Class<?> targetInterface;
    //超时时间
    private int consumeTimeout;
    //调用者线程数
    private static int threadWorkerNumber = 10;
    //负载均衡策略
    private String clusterStrategy;
    private String failMode;
    private final RpcServiceProperties rpcServiceProperties;

    public RevokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy, String failMode) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
        this.failMode = failMode;
        rpcServiceProperties = RpcServiceProperties.builder().group("").version("").build();
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //服务接口名称
        String serviceKey = targetInterface.getName();
        //获取某个接口的服务提供者列表
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consume().get(serviceKey);
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(clusterStrategy);
        ProviderService providerService = clusterStrategyService.select(providerServices);
        System.out.println("本地调用的服务提供者IP和Port：" + providerService.getServerIp() + ":" + providerService.getServerPort());
        //复制一份服务提供者信息
        ProviderService newProvider = providerService.copy();
        //设置本次调用服务的方法以及接口
        newProvider.setServiceMethod(method);
        newProvider.setServiceItf(targetInterface);
        //声明调用RpcRequest对象,RpcRequest表示发起一次调用所包含的信息
        final RpcRequest request = RpcRequest.builder()
                //设置本次调用的方法名称
                .methodName(method.getName())
                .interfaceName(method.getDeclaringClass().getName())
                //设置本次调用的方法参数信息
                .parameters(args)
                //设置本次调用的服务提供者信息
                .providerService(newProvider)
                .paramTypes(method.getParameterTypes())
                //设置本次调用的唯一标识(客户端调用使用UUID也无所谓)
                .requestId(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId())
                //设置本次调用的超时时间
                .invokeTimeout(consumeTimeout)
                .failMode(failMode)
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        //构建用来发起调用的线程池
        if (fixedThreadPool == null) {
            synchronized (RevokerProxyBeanFactory.class) {
                if (null == fixedThreadPool) {
                    fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                }
            }
        }
        //根据服务提供者的ip,port,构建InetSocketAddress对象,标识服务提供者地址
        String serverIp = request.getProviderService().getServerIp();
        int serverPort = request.getProviderService().getServerPort();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
        try {
            //提交本次调用信息到线程池fixedThreadPool,发起调用
            Future<RpcResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(inetSocketAddress, request));
            //获取调用的返回结果(阻塞若干秒等待返回)
            RpcResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if (response != null) {
                return response.getData();
            }
        } catch (Exception e) {
            // 重试策略，重试一次
            if (request.getFailMode().equals(FailMode.FAIL_RETRY.getCode())) {
                log.info("由于出现了超时问题，开始Fail Retry，再次尝试...");
                //提交本次调用信息到线程池fixedThreadPool,发起调用
                Future<RpcResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(inetSocketAddress, request));
                //获取调用的返回结果(阻塞若干秒等待返回)
                RpcResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                if (response != null) {
                    return response.getData();
                }
                // 重新选择一个服务提供者
            } else if (request.getFailMode().equals(FailMode.FAIL_OVER.getCode())) {
                log.info("由于出现了超时问题，开始Fail Over，选择另外一个ProviderService再次尝试...");
                // 重新选择一个非当前出现访问问题的ProviderService,由于现在出问题但是待会儿可能就没问题了，所以就不选择remove，而是直接跳过
                ProviderService curProviderService = providerService;
                while (curProviderService == providerService) {
                    curProviderService = clusterStrategyService.select(providerServices);
                }
                System.out.println("本地调用的服务提供者IP和Port：" + curProviderService.getServerIp() + ":" + curProviderService.getServerPort());
                Future<RpcResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(inetSocketAddress, request));
                RpcResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                if (response != null) {
                    return response.getData();
                }
                //否则就是failFast，直接报错就好了...
            } else {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Object getProxy() {
        // 为目标目标类的目标接口方法生成代理，使用本`InvocationHandler`
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }

    private static volatile RevokerProxyBeanFactory singleton;

    public static RevokerProxyBeanFactory singleton(Class<?> targetInterface, int consumeTimeout, String clusterStrategy, String failMode) throws Exception {
        if (null == singleton) {
            synchronized (RevokerProxyBeanFactory.class) {
                if (null == singleton) {
                    singleton = new RevokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy, failMode);
                }
            }
        }
        return singleton;
    }
}