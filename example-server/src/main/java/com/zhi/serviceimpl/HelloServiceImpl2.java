package com.zhi.serviceimpl;

import com.zhi.annotation.RpcService;
import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:41
 */
@Slf4j
//@RpcService
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }
    /**
     * 服务接口的具体实现
     * @param hello
     * @return
     */
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到：{}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回：{}", result);
        return result;
    }
}