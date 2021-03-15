package com.zhi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description 服务端发布远程服务。
 * @Author WenZhiLuo
 * @Date 2021-02-20 12:08
 */
@Slf4j
public class NettyServerMainXml {
    public static void main(String[] args) throws Exception {

        //发布服务
        new ClassPathXmlApplicationContext("zrpc-server.xml");
        log.info(" 服务发布完成");
    }
}