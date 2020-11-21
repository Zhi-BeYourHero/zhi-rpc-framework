package com.zhi.remoting.transport.netty.client;

import com.zhi.enums.SerializableTypeEnum;
import com.zhi.extension.ExtensionLoader;
import com.zhi.factory.SingletonFactory;
import com.zhi.remoting.constants.RpcConstants;
import com.zhi.remoting.dto.RpcMessage;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.registry.ServiceDiscovery;
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
    private final ChannelProvider channelProvider;
    public NettyClientTransport() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }
    @Override
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
        //构建返回值
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            //放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setData(rpcRequest);
            rpcMessage.setCodec(SerializableTypeEnum.KRYO.getCode());
            rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端向服务端发送消息: [{}]", rpcMessage);
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