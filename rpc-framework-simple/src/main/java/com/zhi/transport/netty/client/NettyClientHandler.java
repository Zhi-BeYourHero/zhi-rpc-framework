package com.zhi.transport.netty.client;

import com.zhi.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

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
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("客户端接收到了消息：{}", rpcResponse);
            //以key值为rpcResponse的AttributeKey对象,类似于 Map 中的 key
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcResponse.getRequestId());
            /*
             * AttributeMap 可以看作是一个Channel的共享数据源
             * AttributeMap 的 key 是 AttributeKey，value 是 Attribute
             * 将服务端的返回结果保存到 AttributeMap 上
             */
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            /*
            * 如果指定的消息实现了{@link ReferenceCounted}，尝试调用{@link ReferenceCounted#release()}。
            * 如果指定的消息没有实现{@link ReferenceCounted}，则此方法不执行任何操作。
            * 减少引用计数{@code 1}，并在引用计数达到时释放该对象
            * */
            ReferenceCountUtil.release(msg);
        }
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