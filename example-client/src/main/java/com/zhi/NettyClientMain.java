package com.zhi;

import com.zhi.transport.RpcClient;
import com.zhi.transport.RpcClientProxy;
import com.zhi.transport.netty.NettyRpcClient;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:50
 */
public class NettyClientMain {
    public static void main(String[] args) {
        //1. 获取客户端类,其成员变量主要由host和port, 和一个sendRpcRequest方法,后面由代理类来代理
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 9999);
        //2. 生成客户端代理类
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        //3. 根据客户端代理类获取我们需要的对象,这样代理方法就能生效
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        //4. 调用helloService的hello方法，就会走rpcClientProxy的invoke方法，构建RPC请求，并调用sendRpcRequest方法
        String hello = helloService.hello(new Hello("人生没有生么值不值得，别总是患得患失，干就完事儿", "罗文智是大帅哥(我不管，我觉得是就是)"));
        System.out.println(hello);
    }
}
