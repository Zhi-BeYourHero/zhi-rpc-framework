package com.zhi;

import com.zhi.annotation.RpcReference;
import com.zhi.api.Hello;
import com.zhi.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-18 22:20
 */
@Component
@Slf4j
public class HelloController {

    @RpcReference
    private HelloService helloService;

    public void test() {
        String hello = this.helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        for (int i = 0; i < 10; i++) {
            log.info("Client 发起调用， 返回结果为：{}", helloService.hello(new Hello("111", "222")));
        }
    }
}
