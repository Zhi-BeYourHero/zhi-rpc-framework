package com.zhi.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 获取单例对象的工厂类
 * 只要构造方法是私有的，那么类就应该定义为final类
 * @Author WenZhiLuo
 * @Date 2020-10-13 15:54
 */
public final class SingletonFactory {
    private static Map<String, Object> objectMap = new HashMap<>();
    private SingletonFactory() {
    }
    public static  <T> T getInstance(Class<T> c) {
        String key = c.toString();
        Object instance = objectMap.get(key);
        synchronized (c) {
            if (instance == null) {
                try {
                    instance = c.newInstance();
                    objectMap.put(key, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return c.cast(instance);
    }
}
