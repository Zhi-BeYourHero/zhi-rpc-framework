package com.zhi;

import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.remoting.socket.RpcServer;

/**
 * @Description 启动rpcServer，并注册对应的服务
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:45
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        //手动注册
        defaultServiceRegistry.register(helloService);
        RpcServer rpcServer = new RpcServer(defaultServiceRegistry);
        rpcServer.start(9999);
    }
}
