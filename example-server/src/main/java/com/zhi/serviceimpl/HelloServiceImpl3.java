package com.zhi.serviceimpl;

import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-20 11:54
 */
@Slf4j
public class HelloServiceImpl3 implements HelloService {

    static {
        System.out.println("HelloServiceImpl3被创建");
    }
    /**
     * 服务接口的具体实现
     * @param hello
     * @return
     */
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl3收到：{}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl3返回：{}", result);
        return result;
    }
}