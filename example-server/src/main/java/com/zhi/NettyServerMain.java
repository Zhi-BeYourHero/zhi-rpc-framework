package com.zhi;

import com.zhi.api.HelloService;
import com.zhi.remoting.transport.netty.server.NettyServer;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
    }
}