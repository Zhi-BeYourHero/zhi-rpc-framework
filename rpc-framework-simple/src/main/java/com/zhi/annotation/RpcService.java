package com.zhi.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Description Rpc service annotation, marked on service implements class
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {
    /**
     * 分组名
     */
    String groupName() default "default";

    /**
     * 权重
     */
    int weight() default 1;

    /**
     * 应用名
     */
    String appKey() default "zrpc";

    /**
     * 工作线程
     */
    int workThreads() default 100;

    /**
     * 调用超时时间
     */
    int timeout() default 3000;
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}