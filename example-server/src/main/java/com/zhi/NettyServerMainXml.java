package com.zhi;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description 服务端发布远程服务。
 * @Author WenZhiLuo
 * @Date 2021-02-20 12:08
 */
public class NettyServerMainXml {
    public static void main(String[] args) throws Exception {

        //发布服务
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("zrpc-server.xml");
        System.out.println(" 服务发布完成");
    }
}