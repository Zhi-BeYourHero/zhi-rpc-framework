package com.zhi.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 自定义消息传输体
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:12
 */
@Data
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
}