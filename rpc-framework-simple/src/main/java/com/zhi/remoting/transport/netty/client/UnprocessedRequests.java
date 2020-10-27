package com.zhi.remoting.transport.netty.client;

import com.zhi.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 未处理的请求
 * @Author WenZhiLuo
 * @Date 2020-10-17 9:38
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES  = new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> completableFuture = UNPROCESSED_RESPONSE_FUTURES .remove(rpcResponse.getRequestId());
        if (completableFuture != null) {
            completableFuture.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}