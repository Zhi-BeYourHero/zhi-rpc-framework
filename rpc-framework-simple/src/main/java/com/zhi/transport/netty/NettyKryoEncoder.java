package com.zhi.transport.netty;

import com.zhi.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 11:17
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private Serializer serializer;
    private Class<?> genericClass;

    /**
     * 将对象转为字节码然后写入到ByteBuf对象中...
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf byteBuf) throws Exception {
        //判断传入的对象是否与指定的类兼容
        if (genericClass.isInstance(obj)) {
            // 1、将对象转为byte
            byte[] bytes = serializer.serialize(obj);
            // 2、读取消息的长度
            int dataLength = bytes.length;
            // 3、写入消息对应的字节数组长度，writeIndex + 4
            byteBuf.writeInt(dataLength);
            // 4、将字节数组写入ByteBuf对象
            byteBuf.writeBytes(bytes);
        }
    }
}