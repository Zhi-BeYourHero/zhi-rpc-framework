package com.zhi.remoting.transport.netty.client;

import com.zhi.enums.SerializableTypeEnum;
import com.zhi.remoting.constants.RpcConstants;
import com.zhi.remoting.dto.RpcMessage;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.revoker.NettyChannelPoolFactory;
import com.zhi.revoker.RevokerResponseHolder;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Description 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * @Author WenZhiLuo
 * @Date 2020-10-11 14:58
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //客户端接收到的服务端响应
            log.info("客户端接收到了消息：{}", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    // 将Netty异步返回的结果存入阻塞队列,以便调用端同步获取(同步服务、而netty是NIO、异步返回的结果，因此根据配置的超时时间来判断结果是否可用)
                    RevokerResponseHolder.putResultValue(rpcResponse);
                }
            }
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //如果是写空闲
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                ArrayBlockingQueue<Channel> blockingQueue = NettyChannelPoolFactory.channelPoolFactoryInstance().acquire(inetSocketAddress);
                Channel channel = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
                try {
                    //若获取的channel通道已经不可用,则重新获取一个
                    while (channel == null || !channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                        log.warn("----------retry get new Channel------------");
                        channel = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (channel == null) {
                            // 若队列中没有可用的Channel,则重新注册一个Channel
                            // 若注册失败，直接抛NPE，会进入catch中，不太合适(后期可以改造下)
                            channel = NettyChannelPoolFactory.channelPoolFactoryInstance().registerChannel(inetSocketAddress);
                        }
                    }
                    RpcMessage rpcMessage = new RpcMessage();
                    rpcMessage.setCodec(SerializableTypeEnum.KRYO.getCode());
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                    rpcMessage.setData(RpcConstants.PING);
                    channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                }finally {
                    //本次调用完毕后,将Netty的通道channel重新释放到队列中,以便下次调用复用
                    NettyChannelPoolFactory.channelPoolFactoryInstance().release(blockingQueue, channel, inetSocketAddress);
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //flush()方法，刷新内存队列，将数据写入到对端。
        ctx.flush();
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端处理器发生错误：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}