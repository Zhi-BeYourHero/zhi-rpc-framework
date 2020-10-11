package com.zhi.transport.netty.codec;

import com.zhi.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * @Description
 * 自定义编码器。负责处理"出站"消息，将消息格式转换字节数组然后写入到字节数据的容器 ByteBuf 对象中。
 * <p>
 * 网络传输需要通过字节流来实现，ByteBuf 可以看作是 Netty 提供的字节数据的容器，使用它会让我们更加方便地处理字节数据。
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