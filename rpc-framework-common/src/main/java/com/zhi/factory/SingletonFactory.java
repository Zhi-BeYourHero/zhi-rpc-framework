package com.zhi.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 获取单例对象的工厂类
 * 只要构造方法是私有的，那么类就应该定义为final类
 * @Author WenZhiLuo
 * @Date 2020-10-13 15:54
 */
public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new HashMap<>();
    private SingletonFactory() {
    }
    public static  <T> T getInstance(Class<T> c) {
        String key = c.toString();
        Object instance = OBJECT_MAP.get(key);
        if (instance == null) {
            synchronized (c) {
                instance = OBJECT_MAP.get(key);
                if (instance == null) {
                    try {
                        instance = c.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key, instance);
                    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return c.cast(instance);
    }
}
