package com.zhi.transport.netty.server;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.registry.DefaultServiceRegistry;
import com.zhi.registry.ServiceRegistry;
import com.zhi.transport.RpcRequestHandler;
import com.zhi.utils.concurrent.ThreadPoolFactory;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @Description Netty中处理RpcRequest的Handler
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * @Author WenZhiLuo
 * @Date 2020-10-11 13:24
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;
    private static ExecutorService threadPool;
    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("netty-server-handler-rpc-pool");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadPool.execute(() -> {
            try {
                //1.获取请求
                RpcRequest rpcRequest = (RpcRequest) msg;
                LOGGER.info(String.format("server receive message: %s", rpcRequest));
                String interfaceName = rpcRequest.getInterfaceName();
                //2.获取对应的服务,通过注册中心获取到目标类（客户端需要调用类）
                Object service = serviceRegistry.getService(interfaceName);
                //3.调用对应的服务,执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest, service);
                //4.输出结果
                LOGGER.info(String.format("server get result: %s", result.toString()));
                //5.服务端响应消息給客户端
                ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result));
                //6.添加关闭事件的监听器
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            } finally {
                /* 确保 ByteBuf 被释放，不然可能会有内存泄露问题
                 * 如果指定的消息实现了{@link ReferenceCounted}，尝试调用{@link ReferenceCounted#release()}。
                 * 如果指定的消息没有实现{@link ReferenceCounted}，则此方法不执行任何操作。
                 * 减少引用计数{@code 1}，并在引用计数达到时释放该对象
                 * */
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}