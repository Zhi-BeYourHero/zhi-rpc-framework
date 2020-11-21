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

    KRYO((byte) 0x01, "kryo");
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