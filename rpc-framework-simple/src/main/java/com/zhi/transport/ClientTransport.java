package com.zhi.transport;

import com.zhi.dto.RpcRequest;

/**
 * @Description 实现了 RpcClient 接口的对象需要具有发送 RpcRequest 的能力
 * 传输 RpcRequest。
 * @Author WenZhiLuo
 * @Date 2020-10-11 9:22
 */
public interface ClientTransport {

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}