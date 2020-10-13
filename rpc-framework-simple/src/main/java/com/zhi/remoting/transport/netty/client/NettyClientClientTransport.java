package com.zhi.remoting.transport.netty.client;

import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.registry.ServiceDiscovery;
import com.zhi.registry.ZkServiceDiscovery;
import com.zhi.remoting.transport.ClientTransport;
import com.zhi.remoting.dto.RpcMessageChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Description 基于 Netty 传输 RpcRequest
 * 进行了重构吧，将原来的一个NettyRpcClient拆分成了ChannelProvider, NettyClient和NettyClientClientTransport
 * 原来Bootstrap的初始化和EventLoopGroup的初始化由NettyClient完成，channel的获取由ChannelProvider完成
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:09
 */
@Slf4j
public class NettyClientClientTransport implements ClientTransport {
    private final ServiceDiscovery serviceDiscovery;
    public NettyClientClientTransport() {
        serviceDiscovery = new ZkServiceDiscovery();
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress);
            if (!channel.isActive()) {
                NettyClient.close();
                return null;
            }
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info(String.format("客户端向服务端发送消息: %s", rpcRequest.toString()));
                } else {
                    log.error("客户端发送消息失败：", future.cause());
                }
            });
            //等待客户端监听端口关闭
            channel.closeFuture().sync();
            //通过AttributeKey获取通道从服务端获取的响应即RpcResponse
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
            RpcResponse rpcResponse = channel.attr(key).get();
            log.info("client get rpcResponse from channel:{}", rpcResponse);
            //校验 RpcRequest 和 RpcResponse
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            result.set(rpcResponse.getData());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return result.get();
    }
}
