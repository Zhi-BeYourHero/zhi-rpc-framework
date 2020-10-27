package com.zhi.proxy;

import com.zhi.entity.RpcServiceProperties;
import com.zhi.remoting.dto.RpcMessageChecker;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.transport.ClientTransport;
import com.zhi.remoting.transport.netty.client.NettyClientTransport;
import com.zhi.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @Description 进行了优化，解耦合，通过代理类为每个RpcRequest随机生成一个requestId
 * 动态代理类。当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法
 * 正是因为动态代理才让客户端调用的远程方法像是调用本地方法一样（屏蔽了中间过程）
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:38
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    /**
     * 用于发送请求给服务端，对应socket和netty两种实现方式
     */
    private final ClientTransport clientTransport;
    private final RpcServiceProperties rpcServiceProperties;
    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        rpcServiceProperties = RpcServiceProperties.builder().group("").version("").build();
    }

    public RpcClientProxy(ClientTransport clientTransport, RpcServiceProperties rpcServiceProperties) {
        this.clientTransport = clientTransport;
        if (rpcServiceProperties.getGroup() == null) {
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null) {
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    /**
     * 通过 Proxy.newProxyInstance() 方法获取某个类的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 当你使用代理对象调用方法的时候实际会调用到这个方法。代理对象就是你通过上面的 getProxy 方法获取到的对象。
     * 通过代理类来构建对应的rpc请求，通过Method类获取方法名称，方法对应的类和方法参数类型，并且设置对应的参数。
     * 通过RpcClient来发送请求
     * java.lang.reflect.Method.getDeclaringClass()方法返回表示声明由此Method对象表示的方法的类的Class对象。
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .interfaceName(method.getDeclaringClass().getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) clientTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        if (clientTransport instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse<Object>) clientTransport.sendRpcRequest(rpcRequest);
        }
        //校验 RpcRequest 和 RpcResponse
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}