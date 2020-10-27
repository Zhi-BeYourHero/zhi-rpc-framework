package com.zhi;

import com.zhi.proxy.RpcClientProxy;
import com.zhi.remoting.transport.ClientTransport;
import com.zhi.remoting.transport.netty.client.NettyClientTransport;

/**
 * @Description 简单写一个方法获取代理对象，其实应该在simple框架里面提供一个接口获取代理对象
 * @Author WenZhiLuo
 * @Date 2020-10-27 9:29
 */
public class ClientProxy {
    public static <T> T getServiceProxy(Class<T> serviceClass) {
        ClientTransport clientTransport = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport);
        return rpcClientProxy.getProxy(serviceClass);
    }
}