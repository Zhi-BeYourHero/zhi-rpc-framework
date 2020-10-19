package com.zhi.config;

import com.zhi.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.zhi.utils.zk.CuratorUtils;
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}