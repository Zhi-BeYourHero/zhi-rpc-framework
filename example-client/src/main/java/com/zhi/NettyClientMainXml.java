package com.zhi;

import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Scanner;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-20 13:08
 */
@Slf4j
public class NettyClientMainXml {
    public static void main(String[] args) throws Exception {

        //引入远程服务
        // 使用`zrpc-client.xml`加载spring上下文，其中定义了一个`bean id = "remoteHelloService"`
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("zrpc-client.xml");
        //获取远程服务
        // 从spring上下文中根据id取出这个bean
        final HelloService helloService = (HelloService) context.getBean("remoteHelloService");

        long count = 1L;

        //调用服务并打印结果
        for (int i = 0; i < count; i++) {
            try {
                String result = helloService.hello(new Hello("Message:小小智无敌", "description：大大智逆天"));
                System.out.println(result);
            } catch (Exception e) {
                log.warn("--------", e);
            }
        }
        //关闭jvm
//        System.exit(0);
        new Scanner(System.in);
    }
}
