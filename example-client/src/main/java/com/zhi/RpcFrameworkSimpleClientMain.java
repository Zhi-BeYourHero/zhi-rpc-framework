package com.zhi;

import com.zhi.remoting.transport.ClientTransport;
import com.zhi.proxy.RpcClientProxy;
import com.zhi.remoting.transport.socket.SocketRpcClient;

/**
 * @Description 测试服务消费者（客户端）
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:51
 */
public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        //经过改进之后，RpcClientProxy与具体的host,port解耦，只与RpcClient耦合...
        ClientTransport clientTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("21345", "786534"));
        System.out.println(hello);
    }
}