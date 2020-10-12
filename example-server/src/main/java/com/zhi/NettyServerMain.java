package com.zhi;

import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.registry.ServiceRegistry;
import com.zhi.transport.netty.server.NettyServer;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
public class NettyServerMain {
    public static void main(String[] args) {
        //这里别用HelloService来进行接收，因为ServiceRegistry会通过实现类去找对应的接口...
        HelloServiceImpl helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        //手动注册
        serviceRegistry.register(helloService);
        NettyServer nettyServer = new NettyServer(9999);
        nettyServer.run();
    }
}