package com.zhi.remoting.transport.netty.server;

import com.zhi.enums.RpcResponseCodeEnum;
import com.zhi.enums.SerializableTypeEnum;
import com.zhi.remoting.constants.RpcConstants;
import com.zhi.remoting.dto.RpcMessage;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.handler.RpcRequestHandler;
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
                if (msg instanceof RpcMessage) {
                    log.info("server receive msg: [{}] ", msg);
                    byte messageType = ((RpcMessage) msg).getMessageType();
                    if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                        RpcMessage rpcMessage = new RpcMessage();
                        rpcMessage.setCodec(SerializableTypeEnum.KRYO.getCode());
                        rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                        rpcMessage.setData(RpcConstants.PONG);
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } else {
                        RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                        // Execute the target method (the method the client needs to execute) and return the method result
                        Object result = rpcRequestHandler.handle(rpcRequest);
                        log.info(String.format("server get result: %s", result.toString()));
                        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                            RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                            RpcMessage rpcMessage = new RpcMessage();
                            rpcMessage.setCodec(SerializableTypeEnum.KRYO.getCode());
                            rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                            rpcMessage.setData(rpcResponse);
                            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } else {
                            RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                            RpcMessage rpcMessage = new RpcMessage();
                            rpcMessage.setCodec(SerializableTypeEnum.KRYO.getCode());
                            rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                            rpcMessage.setData(rpcResponse);
                            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                            log.error("not writable now, message dropped");
                        }
                    }
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