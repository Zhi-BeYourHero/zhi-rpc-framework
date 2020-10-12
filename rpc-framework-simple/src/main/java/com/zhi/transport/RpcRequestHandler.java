package com.zhi.transport;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.enumeration.RpcResponseCode;
import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description RpcRequest处理类
 * 优化①：原来的Service是作为handle方法的参数传进来的，在NettyServerHandler里通过注册中心获取的，
 * 现在移到Handler来了...
 * @Author WenZhiLuo
 * @Date 2020-10-10 14:40
 */
public class RpcRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcRequestHandler.class);
    private static final ServiceRegistry SERVICE_REGISTRY;
    static {
        SERVICE_REGISTRY = new DefaultServiceRegistry();
    }

    /**
     * 处理rpcRequest并返回方法执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        Object result = null;
        //通过注册中心获取到目标类（客户端需要调用类）
        Object service = SERVICE_REGISTRY.getService(rpcRequest.getInterfaceName());
        try {
            result = invokeTargetMethod(rpcRequest, service);
            LOGGER.info("service:{} successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("occur exception", e);
        }
        return result;
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        if (method == null) {
            return RpcResponse.fail(RpcResponseCode.FAIL);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}