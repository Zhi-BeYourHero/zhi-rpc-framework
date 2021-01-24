package com.zhi.annotation;

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

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}