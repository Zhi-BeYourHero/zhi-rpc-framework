package com.zhi.serialize;

/**
 * @Description 序列化接口，所有序列化类都要实现这个接口
 * @Author WenZhiLuo
 * @Date 2020-10-11 10:29
 */
public interface Serializer {
    /**
     * 序列化
     * @param obj 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 要反序列化成的类
     * @param <T>
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}