package com.zhi.transport.netty;

import com.zhi.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 14:58
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //客户端接收到的服务端响应
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("客户端接收到了消息：{}", rpcResponse);
            //以key值为rpcResponse的AttributeKey对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            // 将服务端的返回结果保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源
            // AttributeMap的key是AttributeKey，value是Attribute
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端处理器发生错误...");
        cause.printStackTrace();
        ctx.close();
    }
}