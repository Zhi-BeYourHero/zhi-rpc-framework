package com.zhi.remoting.transport.netty.server;

import com.zhi.enumeration.RpcMessageTypeEnum;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.handler.RpcRequestHandler;
import com.zhi.factory.SingletonFactory;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

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

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                //1.获取请求
                log.info("server receive msg: [{}] ", msg);
                RpcRequest rpcRequest = (RpcRequest) msg;
                if (rpcRequest.getRpcMessageTypeEnum() == RpcMessageTypeEnum.HEART_BEAT) {
                    log.info("receive heat beat msg from client");
                    return;
                }
                //3.调用对应的服务,执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                //4.输出结果
                log.info(String.format("server get result: %s", result.toString()));
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    //返回方法执行结果给客户端
                    //添加监听器，
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    /**
                     * A {@link ChannelFutureListener} that closes the {@link Channel} when the
                     * operation ended up with a failure or cancellation rather than a success.
                     */
                    ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    log.error("not writable now, message dropped");
                }
            } finally {
                /* 确保 ByteBuf 被释放，不然可能会有内存泄露问题
                 * 如果指定的消息实现了{@link ReferenceCounted}，尝试调用{@link ReferenceCounted#release()}。
                 * 如果指定的消息没有实现{@link ReferenceCounted}，则此方法不执行任何操作。
                 * 减少引用计数{@code 1}，并在引用计数达到时释放该对象
                 * */
                ReferenceCountUtil.release(msg);
            }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}