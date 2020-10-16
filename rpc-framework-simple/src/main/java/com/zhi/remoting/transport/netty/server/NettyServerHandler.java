package com.zhi.remoting.transport.netty.server;

import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.handler.RpcRequestHandler;
import com.zhi.utils.concurrent.ThreadPoolFactoryUtils;
import com.zhi.factory.SingletonFactory;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.threadPool = ThreadPoolFactoryUtils.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadPool.execute(() -> {
            try {
                //1.获取请求
                RpcRequest rpcRequest = (RpcRequest) msg;
                log.info(String.format("server receive message: %s", rpcRequest));
                String interfaceName = rpcRequest.getInterfaceName();
                //3.调用对应的服务,执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                //4.输出结果
                log.info(String.format("server get result: %s", result.toString()));
                //5.服务端响应消息給客户端
                ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
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
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}