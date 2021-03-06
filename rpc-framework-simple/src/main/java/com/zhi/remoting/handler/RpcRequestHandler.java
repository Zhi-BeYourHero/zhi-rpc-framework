package com.zhi.remoting.handler;

import com.zhi.entity.RpcServiceProperties;
import com.zhi.factory.SingletonFactory;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.exception.RpcException;
import com.zhi.provider.ServiceProvider;
import com.zhi.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    //TODO 使用static初始化资源的好处，为什么Guide老哥一下子用又一下子不用?
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }
    /**
     * 处理rpcRequest：调用对应的方法，并返回方法执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group(rpcRequest.getGroup()).version(rpcRequest.getVersion())
                .serviceName(rpcRequest.getInterfaceName()).build();
        //通过注册中心获取到目标类（客户端需要调用类）
        Object service = serviceProvider.getService(rpcServiceProperties);
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     * @param rpcRequest 客户端请求
     * @param service    提供服务的对象
     * @return 目标方法执行的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}