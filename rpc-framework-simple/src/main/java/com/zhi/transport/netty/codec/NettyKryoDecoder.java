package com.zhi.transport.netty.codec;

import com.zhi.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @Description todo 这个类多研究一下
 * 自定义解码器。负责处理"入站"消息，将消息格式转换为我们需要的业务对象
 * @Author WenZhiLuo
 * @Date 2020-10-11 11:38
 */
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyKryoDecoder.class);
    private Serializer serializer;
    private Class<?> genericClass;
    /**
     * Netty传输的消息长度，也就是对象序列化后的字节数组的大小，存储在ByteBuf头部
     * todo 为什么是4？
     */
    private static final int BODY_LENGTH = 4;

    /**
     * 解码 ByteBuf 对象
     *
     * @param ctx 解码器关联的 ChannelHandlerContext 对象
     * @param in  "入站"数据，也就是 ByteBuf 对象
     * @param out 解码之后的数据对象需要添加到 out 对象里面
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //1. byteBuf中写入的消息长度所占的字节数已经是4了，所以byteBuf的可读字节必须大于4
        if (in.readableBytes() >= BODY_LENGTH) {
            //2.标记当前的readerIndex的位置，以便后面重置readerIndex的时候使用
            in.markReaderIndex();
            //3. 读取消息的长度，消息的长度是我们encode的时候自己写入的
            int dataLength = in.readInt();
            //4.遇到不合理的情况直接return,讲真，我觉得第二个判断条件没必要，因为上一个if已经是>=4了
            if (dataLength < 0 || in.readableBytes() < 0) {
                return;
            }
            //5.如果可读字节数小于消息长度的话，说明消息消息是不完整的，重置readerIndex
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }
            //6.走到这里说明没什么问题了，可以序列化了...
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object deserialize = serializer.deserialize(body, genericClass);
            out.add(deserialize);
        }
    }
}