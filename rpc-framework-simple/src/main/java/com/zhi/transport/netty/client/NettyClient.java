package com.zhi.transport.netty.client;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.serialize.kyro.KryoSerializer;
import com.zhi.transport.netty.codec.NettyKryoDecoder;
import com.zhi.transport.netty.codec.NettyKryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 负责BOOTSTRAP和EVENT_LOOP_GROUP的初始化
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:03
 */
@Slf4j
public final class NettyClient {
    private static final Bootstrap BOOTSTRAP;
    private static final EventLoopGroup EVENT_LOOP_GROUP;

    // 初始化相关资源比如 EventLoopGroup、Bootstrap
    static {
        EVENT_LOOP_GROUP = new NioEventLoopGroup();
        BOOTSTRAP = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        BOOTSTRAP.group(EVENT_LOOP_GROUP)
                .channel(NioSocketChannel.class)
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        /*自定义序列化编解码器*/
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        // ByteBuf -> RpcRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    private NettyClient() {
    }

    public static void close() {
        log.info("call close method");
        EVENT_LOOP_GROUP.shutdownGracefully();
    }

    public static Bootstrap initializeBootstrap() {
        return BOOTSTRAP;
    }
}
