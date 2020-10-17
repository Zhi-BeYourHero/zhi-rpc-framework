package com.zhi.remoting.transport.netty.client;

import com.zhi.factory.SingletonFactory;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.registry.ServiceDiscovery;
import com.zhi.registry.ZkServiceDiscovery;
import com.zhi.remoting.transport.ClientTransport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @Description 基于 Netty 传输 RpcRequest
 * 进行了重构吧，将原来的一个NettyRpcClient拆分成了ChannelProvider, NettyClient和NettyClientClientTransport
 * 原来Bootstrap的初始化和EventLoopGroup的初始化由NettyClient完成，channel的获取由ChannelProvider完成
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:09
 */
@Slf4j
public class NettyClientTransport implements ClientTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    public NettyClientTransport() {
        serviceDiscovery = new ZkServiceDiscovery();
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }
    @Override
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
        //构建返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        Channel channel = ChannelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            //放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端向服务端发送消息: {}", rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("客户端发送消息失败：", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }
}