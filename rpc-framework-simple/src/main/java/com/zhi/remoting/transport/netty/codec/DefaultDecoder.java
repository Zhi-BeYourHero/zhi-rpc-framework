package com.zhi.remoting.transport.netty.codec;

import com.zhi.extension.ExtensionLoader;
import com.zhi.remoting.transport.netty.codec.enums.SerializableEnum;
import com.zhi.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Description todo 这个类多研究一下
 * 協議格式
 *    | length | serializable id | body length | body data |
 *    |    1   |        2        |       3     |      4    |
 *
 *    1、大端4字節整數，等于2、3、4长度总和
 *    2、大端4字節整數，序列化方法的序號
 *    3、body 長度  大端4字節整數，具體為4的長度
 *    4、body 內容
 * 自定义解码器。负责处理"入站"消息，将消息格式转换为我们需要的业务对象
 * @Author WenZhiLuo
 * @Date 2020-10-11 11:38
 */
@AllArgsConstructor
@Slf4j
public class DefaultDecoder extends ByteToMessageDecoder {
    private final Class<?> genericClass;
    private static final HashMap<Integer, String> SERIALIZABLE_MAP;

    static {
        SERIALIZABLE_MAP = new HashMap<>();
        Arrays.stream(SerializableEnum.values()).forEach(ser -> {
            SERIALIZABLE_MAP.put(ser.getSerializableId(), ser.getSerializableKey());
        });
    }
    /**
     * Netty传输的消息长度，也就是对象序列化后的字节数组的大小，存储在ByteBuf头部
     * todo 为什么是4？
     */
    private static final int TOTAL_LENGTH = 12;

    /**
     * 解码 ByteBuf 对象
     *
     * @param ctx 解码器关联的 ChannelHandlerContext 对象
     * @param in  "入站"数据，也就是 ByteBuf 对象
     * @param out 解码之后的数据对象需要添加到 out 对象里面
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //1. byteBuf中写入的消息长度所占的字节数已经是12了，所以byteBuf的可读字节必须大于12
        if (in.readableBytes() >= TOTAL_LENGTH) {
            //2. 标记当前的readerIndex的位置，以便后面重置readerIndex的时候使用
            in.markReaderIndex();
            //3. 读取消息的长度，消息的长度是我们encode的时候自己写入的
            int totalLength  = in.readInt();
            //4. 遇到不合理的情况直接return,讲真，我觉得第二个判断条件没必要，因为上一个if已经是>=4了
            if (totalLength  < 0 || in.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            //5. 如果可读字节数小于消息长度的话，说明消息消息是不完整的，重置readerIndex
            if (in.readableBytes() < totalLength) {
                in.resetReaderIndex();
                return;
            }
            //6. 讀取選擇序列化方式.
            int serializableId = in.readInt();
            String serializerKey = SERIALIZABLE_MAP.get(serializableId);
            // 7.不支持的序列化方式，返回
            if (StringUtil.isNullOrEmpty(serializerKey)) {
                in.resetReaderIndex();
                return;
            }
            // 8.選擇序列化器
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerKey);
            // 9.開始讀取body長度
            int dataLength = in.readInt();
            // 10.開始序列化
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object deserialize = serializer.deserialize(body, genericClass);
            out.add(deserialize);
            log.info("successful decode ByteBuf to Object");
        }
    }
}