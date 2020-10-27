package com.zhi;

import com.zhi.api.HelloService;
import com.zhi.remoting.transport.socket.SocketRpcServer;

/**
 * @Description 测试用服务提供方（服务端）
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:45
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9999);
        socketRpcServer.publishService(helloService, HelloService.class);
    }
}