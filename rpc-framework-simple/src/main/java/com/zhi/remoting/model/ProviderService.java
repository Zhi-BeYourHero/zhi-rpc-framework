package com.zhi.remoting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @Description 服务注册中心的服务提供者注册信息
 * @Author WenZhiLuo
 * @Date 2021-02-18 20:18
 */
@Setter
@Getter
public class ProviderService implements Serializable {
    /** 服务接口的类字面变量 */
    private Class<?> serviceItf;
    /** transient 关键字修饰的变量是不会被序列化的 */
    private transient Object serviceObject;
    @JsonIgnore
    private transient Method serviceMethod;
    private String rpcServiceName;
    /** 服务所在IP地址 */
    private String serverIp;
    /** 服务所在端口号 */
    private int serverPort;
    /** 服务调用超时时间 */
    private long timeout;
    //该服务提供者权重
    private int weight;
    //服务端线程数
    private int workerThreads;
    //服务提供者唯一标识(应用名)
    private String appKey;
    //服务分组组名
    private String groupName;

    public ProviderService copy() {
        ProviderService providerService = new ProviderService();
        providerService.setServiceItf(serviceItf);
        providerService.setServiceObject(serviceObject);
        providerService.setServiceMethod(serviceMethod);
        providerService.setServerIp(serverIp);
        providerService.setServerPort(serverPort);
        providerService.setTimeout(timeout);
        providerService.setWeight(weight);
        providerService.setWorkerThreads(workerThreads);
        providerService.setAppKey(appKey);
        providerService.setGroupName(groupName);
        providerService.setRpcServiceName(rpcServiceName);
        return providerService;
    }
}