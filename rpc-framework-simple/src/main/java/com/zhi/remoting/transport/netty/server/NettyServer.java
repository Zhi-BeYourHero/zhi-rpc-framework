package com.zhi.remoting.transport.netty.server;

import com.zhi.config.CustomShutdownHook;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.transport.netty.codec.kryo.NettyKryoDecoder;
import com.zhi.remoting.transport.netty.codec.kryo.NettyKryoEncoder;
import com.zhi.serialize.kryo.KryoSerializer;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Description 服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 * @Author WenZhiLuo
 * @Date 2020-10-11 9:34
 */
@Slf4j
@Component
public class NettyServer {
    //TODO 话说为什么这个变量应该是final的？。。。 然后又删了,又加回去了额
    private final KryoSerializer kryoSerializer = new KryoSerializer();
    public static final int PORT = 9998;

    @SneakyThrows
    public void start() {
        //这个钩子的添加从start()方法末尾改到前面，然后又让类实现InitializingBean的afterPropertiesSet方法中调用->最后又放到头...当服务端(provider)关闭时候做一些事情，比如说取消注册所有服务
        //由此看出Guide🤔了很多，但放在start确实是最佳实践
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    //设置server通道类型
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    // 设置tcp缓冲区，TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，
                    // 就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
                    // Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //初始化服务器可连接队列大小，服务器处理客户端连接请求是顺序处理的，所以同一时间只能一个客户端连接
                    //多个客户端来的时候，服务端将不能处理的请求放到队列中排队
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //一直保持连接状态...即是否开启 TCP 底层心跳机制
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            //添加一些处理器
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            //绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(host, PORT).sync();
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server：", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}