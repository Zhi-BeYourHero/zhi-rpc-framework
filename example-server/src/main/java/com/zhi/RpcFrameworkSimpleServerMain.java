package com.zhi;

/**
 * @Description 启动rpcServer，并注册对应的服务
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:45
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9999);
        // TODO 修改实现方式，通过map存放service解决只能注册一个service
        System.out.println("后面的不会执行");
        rpcServer.register(new HelloServiceImpl(), 9999);
    }
}
