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

    /**
     * 通过CompletableFuture异步接收返回结果，在sendRpcRequest的时候将其put到UNPROCESSED_RESPONSE_FUTURES的Map中
     * 当通过{@link NettyClientHandler}的ChannelRead方法接收到返回结果时，将调用这个方法
     * @param rpcResponse
     */
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> completableFuture = UNPROCESSED_RESPONSE_FUTURES .remove(rpcResponse.getRequestId());
        if (completableFuture != null) {
            //表示异步接收结果，接收结果为rpcResponse，将其设置到completableFuture中
            completableFuture.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}