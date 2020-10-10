package com.zhi.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 13:43
 */
@AllArgsConstructor
@Getter
@ToString
public enum  RpcErrorMessageEnum {
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_NULL("注册的服务不能为空");
    //枚举类是天然单例，避免字段值修改为其他值，所以最好用final修饰。
    private final String message;
}