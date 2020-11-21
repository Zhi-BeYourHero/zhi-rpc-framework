package com.zhi.remoting.transport.netty.codec.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-21 12:00
 */
@Getter
@AllArgsConstructor
public enum SerializableEnum {
    KRYO(1, "kryo");
    private Integer serializableId;
    private String serializableKey;
}