package com.zhi;

import com.zhi.api.HelloService;
import com.zhi.remoting.transport.netty.server.NettyServer;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9998);
        nettyServer.publishService(helloService, HelloService.class);
    }
}