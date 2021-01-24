package com.zhi.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.exception.SerializeException;
import com.zhi.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Description Kryo序列化类，Kryo序列化效率很高，但是只兼容 Java 语言
 * @Author WenZhiLuo
 * @Date 2020-10-11 10:33
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * 由于 Kryo 不是线程安全的。每个线程都应该有自己的 Kryo，Input 和 Output 实例。
     * 所以，使用 ThreadLocal 存放 Kryo 对象
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        //默认值为true, 是否关闭注册行为，关闭之后可能存在序列化问题，一般推荐设置为true
        kryo.setReferences(true);
        //默认值为false,是否关闭循环依赖,可以提高性能,但是一般不推荐设置为true;
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    /**
     * @param obj 要序列化的对象
     * @return
     */
    @Override
    public <T> byte[] serialize(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            //Object -> byte：将对象序列化为byte数组
            kryo.writeObject(output, obj);
            KRYO_THREAD_LOCAL.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            // byte -> Object：从byte数组中反序列化出对象
            Object obj = kryo.readObject(input, clazz);
            KRYO_THREAD_LOCAL.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            //既然要直接throw exception了，那自然也没有必要去手动再打日志了..
            throw new SerializeException("反序列化失败");
        }
    }
}