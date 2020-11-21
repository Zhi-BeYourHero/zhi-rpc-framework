package com.zhi.spring;

import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import com.zhi.spring.annotation.RpcServiceScan;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 10:51
 */
public class ClientTest {

    @Test
    public void test() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(TestConfig.class);
        applicationContext.refresh();
        applicationContext.start();

        HelloService helloService = applicationContext.getBean(HelloService.class);
        Hello hello = Hello.builder().message("test message").description("test description").build();
        String res = helloService.hello(hello);
        String expectedResult = "Hello description is " + hello.getDescription();
        Assert.assertEquals(expectedResult, res);

    }

    @RpcServiceScan("com.zhi.api")
    public static class TestConfig {

    }
}
