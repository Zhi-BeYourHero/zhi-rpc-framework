package com.zhi.config;

import com.zhi.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.zhi.registry.zk.uril.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 当服务端(provider)关闭时候做一些事情，比如说取消注册所有服务
 * 从关闭特定线程池 -> 关闭所有
 * @Author WenZhiLuo
 * @Date 2020-10-16 21:50
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();
    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }
    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        //在JVM销毁前执行的一个线程.当然这个线程依然要自己写.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry(CuratorUtils.getZkClient());
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}