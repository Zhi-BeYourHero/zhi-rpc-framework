package com.zhi.remoting.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-18 21:00
 */
@Setter
@Getter
public class InvokerService implements Serializable {
    private Class<?> serviceItf;
    private Object serviceObject;
    private Method serviceMethod;
    private String invokerIp;
    private int invokerPort;
    private long timeout;
    //服务提供者唯一标识
    private String remoteAppKey;
    //服务分组组名
    private String groupName = "default";
}