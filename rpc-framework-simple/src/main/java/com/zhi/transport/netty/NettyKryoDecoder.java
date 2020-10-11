package com.zhi.transport.netty;

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
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        //1. byteBuf中写入的消息长度所占的字节数已经是4了，所以byteBuf的可读字节必须大于4
        if (byteBuf.readableBytes() >= BODY_LENGTH) {
            //2.标记当前的readerIndex的位置，以便后面重置readerIndex的时候使用
            byteBuf.markReaderIndex();
            //3. 读取消息的长度，消息的长度是我们encode的时候自己写入的
            int dataLength = byteBuf.readInt();
            //4.遇到不合理的情况直接return,讲真，我觉得第二个判断条件没必要，因为上一个if已经是>=4了
            if (dataLength < 0 || byteBuf.readableBytes() < 0) {
                return;
            }
            //5.如果可读字节数小于消息长度的话，说明消息消息是不完整的，重置readerIndex
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }
            //6.走到这里说明没什么问题了，可以序列化了...
            byte[] body = new byte[dataLength];
            byteBuf.readBytes(body);
            Object deserialize = serializer.deserialize(body, genericClass);
            list.add(deserialize);
        }
    }
}