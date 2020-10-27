package com.zhi;

import com.zhi.spring.annotation.RpcServiceScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 10:50
 */
@Configuration
@RpcServiceScan("com.zhi.spring.service")
public class Config {
}
