package com.zhi.api;

import com.zhi.extension.SPI;

/**
 * @Description 服务类接口，自定义服务接口
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:22
 */
@SPI
public interface HelloService {
    String hello(Hello hello);
}
