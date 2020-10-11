package com.zhi;

import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.transport.socket.SocketRpcServer;

/**
 * @Description 测试用服务提供方（服务端）
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:45
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        //手动注册
        //DefaultServiceRegistry中的SERVICE_MAP和REGISTERED_SERVICE是static的，属于类级别，所有对象都共享的...
        defaultServiceRegistry.register(helloService);
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        socketRpcServer.start(9999);
    }
}