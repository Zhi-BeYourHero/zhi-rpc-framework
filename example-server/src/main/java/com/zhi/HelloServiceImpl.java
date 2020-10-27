package com.zhi;

import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:41
 */
public class HelloServiceImpl implements HelloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServiceImpl.class);

    /**
     * 服务接口的具体实现
     * @param hello
     * @return
     */
    @Override
    public String hello(Hello hello) {
        LOGGER.info("HelloServiceImpl收到：{}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        LOGGER.info("HelloServiceImpl返回：{}", result);
        return result;
    }
}