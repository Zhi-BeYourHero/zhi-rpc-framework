package com.zhi.remoting.transport.netty.codec;

import com.zhi.enums.SerializableTypeEnum;
import com.zhi.extension.ExtensionLoader;
import com.zhi.remoting.constants.RpcConstants;
import com.zhi.remoting.dto.RpcMessage;
import com.zhi.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-21 13:16
 *
 * 自定义协议解码器
 *
 *  * <pre>
 *  * 0     1     2     3     4        5     6     7     8     9          10       11     12    13    14   15
 *  * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+-----------+-----+-----+-----+
 *  * |   magic   code        |version | Full length         | messageType| codec| RequestId                   |
 *  * +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  * |                                                                                                       |
 *  * |                                         body                                                          |
 *  * |                                                                                                       |
 *  * |                                        ... ...                                                        |
 *  * +-------------------------------------------------------------------------------------------------------+
 *
 *  自定义编码器
 *  4B  magic   code 魔法数
 *  1B version 版本
 *  4B full length  消息长度
 *  1B messageType 消息类型
 *  1B codec 序列化
 *  4B  requestId 请求的Id
 *  body object类型数据
 *@see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            // 写入magic数字
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留出位置写入数据包的长度
            out.writerIndex(out.writerIndex() + 4);
            //设置消息类型
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            //设置序列化
            out.writeByte(rpcMessage.getCodec());
            // todo ?这个有什用？
            out.writeInt(ATOMIC_INTEGER.getAndDecrement());
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            //不是心跳
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                //对象序列化
                String codecName = SerializableTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                //消息头长度+内容长度
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }

            int writeIndex = out.writerIndex();
            //1是版本号长度
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            //写入长度
            out.writeInt(fullLength);
            //重置
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}