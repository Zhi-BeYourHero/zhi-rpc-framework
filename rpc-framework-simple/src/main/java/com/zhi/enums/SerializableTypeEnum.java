package com.zhi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-21 13:14
 */
@AllArgsConstructor
@Getter
public enum SerializableTypeEnum {
    JAVA((byte) 0x00, "java"),
    KRYO((byte) 0x01, "kryo"),
    HESSIAN((byte) 0x02, "hessian"),
    PROTOSTUFF((byte) 0x03, "protostuff");
    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializableTypeEnum c : SerializableTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}