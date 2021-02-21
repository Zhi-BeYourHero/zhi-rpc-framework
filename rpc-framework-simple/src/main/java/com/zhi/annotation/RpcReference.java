package com.zhi.annotation;

import com.zhi.cluster.failmode.FailMode;

import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 *
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    int timeout() default 3000;
    /**
     * 负载均衡策略
     */
    String clusterStrategy() default "Polling";
    /**
     * 服务提供者唯一标识
     */
    String remoteAppKey() default "zrpc";
    /**
     * 服务分组组名
     */
    String groupName() default "default";
    String failMode() default "failFast";
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}