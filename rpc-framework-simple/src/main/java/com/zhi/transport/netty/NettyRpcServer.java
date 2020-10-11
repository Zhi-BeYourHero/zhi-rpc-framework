package com.zhi.transport.netty;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.serialize.kyro.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 9:34
 */
public class NettyRpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcServer.class);
    private final int port;
    private KryoSerializer kryoSerializer;

    public NettyRpcServer(int port) {
        this.port = port;
        kryoSerializer = new KryoSerializer();
    }
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    //设置server通道类型
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //添加一些处理器
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    //设置tcp缓冲区，TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，
                    // 就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
                    //Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //初始化服务器可连接队列大小，服务器处理客户端连接请求是顺序处理的，所以同一时间只能一个客户端连接
                    //多个客户端来的时候，服务端将不能处理的请求放到队列中排队
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //一直保持连接状态...
                    .option(ChannelOption.SO_KEEPALIVE, true);
            //绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("occur exception when start server：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}