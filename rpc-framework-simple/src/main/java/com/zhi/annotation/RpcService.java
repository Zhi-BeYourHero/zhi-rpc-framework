package com.zhi.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Description
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
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
