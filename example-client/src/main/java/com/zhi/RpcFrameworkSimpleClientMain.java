package com.zhi;

import com.zhi.transport.RpcClient;
import com.zhi.transport.RpcClientProxy;
import com.zhi.transport.socket.SocketRpcClient;

/**
 * @Description 测试服务消费者（客户端）
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:51
 */
public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        //经过改进之后，RpcClientProxy与具体的host,port解耦，只与RpcClient耦合...
        RpcClient rpcClient = new SocketRpcClient("127.0.0.1", 9999);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("21345", "786534"));
        System.out.println(hello);
    }
}