package com.zhi.remoting.transport.netty.client;

import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.transport.netty.codec.kryo.NettyKryoEncoder;
import com.zhi.serialize.kryo.KryoSerializer;
import com.zhi.remoting.transport.ClientTransport;
import com.zhi.remoting.transport.netty.codec.DefaultDecoder;
import com.zhi.remoting.dto.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 客户端。发送消息到服务端，并接收服务端返回的方法执行结果
 * @Author WenZhiLuo
 * @Date 2020-10-11 14:37
 */
public class NettyRpcClient implements ClientTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcClient.class);
    private String host;
    private int port;
    private static final Bootstrap BOOTSTRAP;

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //初始化相关资源，比如EventLoopGroup, Bootstrap
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        BOOTSTRAP = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        BOOTSTRAP.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        /*自定义序列化解码器*/
                        ch.pipeline().addLast(new DefaultDecoder(RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            //建立连接，同步阻塞直至成功
            ChannelFuture channelFuture = BOOTSTRAP.connect(host, port).sync();
            LOGGER.info("成功建立连接：{}", host + ":" + port);
            Channel channel = channelFuture.channel();
            if (channel != null) {
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        LOGGER.info(String.format("客户端向服务端发送消息: %s", rpcRequest.toString()));
                    } else {
                        LOGGER.error("客户端发送消息失败：", future.cause());
                    }
                });
                //等待客户端监听端口关闭
                channel.closeFuture().sync();
                //通过AttributeKey获取通道从服务端获取的响应即RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                LOGGER.info("client get rpcResponse from channel:{}", rpcResponse);
                //校验 RpcRequest 和 RpcResponse
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            LOGGER.error("当客户端和服务端建立连接时发生异常...");
        }
        return null;
    }
}
