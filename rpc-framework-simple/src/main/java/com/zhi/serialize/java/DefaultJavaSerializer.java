package com.zhi.serialize.java;

import com.zhi.exception.SerializeException;
import com.zhi.serialize.Serializer;

import java.io.*;

/**
 * @Description Java默认的序列化方式
 * @Author WenZhiLuo
 * @Date 2021-01-24 16:04
 */
public class DefaultJavaSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
        } catch (IOException ioException) {
            throw new SerializeException("序列化失败！");
        }
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (T) objectInputStream.readObject();
        } catch (Exception Exception) {
            throw new SerializeException("反序列化失败");
        }
    }
}
