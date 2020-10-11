package com.zhi.transport;

import com.zhi.dto.RpcRequest;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 9:22
 */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}