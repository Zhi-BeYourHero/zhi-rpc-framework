package com.zhi;

import com.zhi.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-18 22:20
 */
@RpcScan(basePackage = {"com.zhi"})
public class NettyClientMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
        helloController.queryAllProviderAndConsumer();
    }
}