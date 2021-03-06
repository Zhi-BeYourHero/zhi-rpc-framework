package com.zhi;

import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import com.zhi.entity.RpcServiceProperties;
import com.zhi.proxy.RpcClientProxy;
import com.zhi.remoting.transport.ClientTransport;
import com.zhi.remoting.transport.netty.client.NettyClientTransport;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:50
 */
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        //1. 获取客户端类,其成员变量主要由host和port, 和一个sendRpcRequest方法,后面由代理类来代理
        //由客户端指定host和port转为服务端指定，客户端获取service
        ClientTransport clientTransport = new NettyClientTransport();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group("test3")
                .version("version3").build();
        //2. 生成客户端代理类
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport, rpcServiceProperties);
        //3. 根据客户端代理类获取我们需要的代理对象,这样代理方法就能生效
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        //4. 调用helloService的hello方法，就会走rpcClientProxy的invoke方法，构建RPC请求，并调用sendRpcRequest方法
        String hello = helloService.hello(new Hello("人生莫得肾么值不值得，别总是患得患失，干就完事儿了！", "罗文智是大帅哥(我不管，我觉得是就是)"));
        System.out.println(hello);
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        String hello2 = helloService.hello(new Hello("111", "222"));
        assert "Hello description is 222".equals(hello2);
    }
}