package com.zhi.remoting.transport.netty.client;

import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.transport.netty.codec.kryo.NettyKryoDecoder;
import com.zhi.remoting.transport.netty.codec.kryo.NettyKryoEncoder;
import com.zhi.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description 负责BOOTSTRAP和EVENT_LOOP_GROUP的初始化
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:03
 */
@Slf4j
public final class NettyClient {
    private static Bootstrap bootstrap;
    private static EventLoopGroup eventLoopGroup;

    // 初始化相关资源比如 EventLoopGroup、Bootstrap
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                        /**
                         * @param readerIdleTime
                         *        an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
                         *        will be triggered when no read was performed for the specified
                         *        period of time.  Specify {@code 0} to disable.
                         **/
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        /*自定义序列化编解码器*/
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        // ByteBuf -> RpcRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功!");
                completableFuture.complete(future.channel());
            }
        });
        return completableFuture.get();
    }
    public static void close() {
        log.info("call close method");
        eventLoopGroup.shutdownGracefully();
    }
}
