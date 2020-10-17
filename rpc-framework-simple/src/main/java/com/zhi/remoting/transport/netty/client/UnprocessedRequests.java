package com.zhi.remoting.transport.netty.client;

import com.zhi.remoting.dto.RpcResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 未处理的请求
 * @Author WenZhiLuo
 * @Date 2020-10-17 9:38
 */
public class UnprocessedRequests {
    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedRequests = new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedRequests.put(requestId, future);
    }
    public void remove(String requestId) {
        unprocessedRequests.remove(requestId);
    }
    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> completableFuture = unprocessedRequests.remove(rpcResponse.getRequestId());
        if (completableFuture != null) {
            completableFuture.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}