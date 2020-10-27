package com.zhi.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 9:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcServiceScannerRegistrar.class)
public @interface RpcServiceScan {
    String value();
}
