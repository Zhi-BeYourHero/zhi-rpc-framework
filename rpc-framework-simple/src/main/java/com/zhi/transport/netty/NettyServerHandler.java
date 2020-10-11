package com.zhi.transport.netty;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.registry.ServiceRegistry;
import com.zhi.transport.RpcRequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description Netty中处理RpcRequest的Handler
 * @Author WenZhiLuo
 * @Date 2020-10-11 13:24
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;
    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //1.获取请求
            RpcRequest rpcRequest = (RpcRequest) msg;
            LOGGER.info(String.format("server receive message: %s", rpcRequest));
            String interfaceName = rpcRequest.getInterfaceName();
            //2.获取对应的服务
            Object service = serviceRegistry.getService(interfaceName);
            //3.调用对应的服务
            Object result = rpcRequestHandler.handle(rpcRequest, service);
            //4.输出结果
            LOGGER.info(String.format("server get result: %s", result.toString()));
            //5.服务端响应消息給客户端
            ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result));
            //6.添加关闭时间的监听器
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            /*
             * 如果指定的消息实现了{@link ReferenceCounted}，尝试调用{@link ReferenceCounted#release()}。
             * 如果指定的消息没有实现{@link ReferenceCounted}，则此方法不执行任何操作。
             * 减少引用计数{@code 1}，并在引用计数达到时释放该对象
             * */
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}