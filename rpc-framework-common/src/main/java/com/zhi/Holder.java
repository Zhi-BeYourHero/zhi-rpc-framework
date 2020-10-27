package com.zhi;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 20:40
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}