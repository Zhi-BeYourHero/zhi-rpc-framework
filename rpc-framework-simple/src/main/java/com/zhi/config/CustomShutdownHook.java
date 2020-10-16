package com.zhi.config;

import com.zhi.utils.concurrent.ThreadPoolFactoryUtils;
import com.zhi.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ExecutorService;

/**
 * @Description 当服务端(provider)关闭时候做一些事情，比如说取消注册所有服务
 * @Author WenZhiLuo
 * @Date 2020-10-16 21:50
 */
@Slf4j
public class CustomShutdownHook {
    private final ExecutorService threadPool = ThreadPoolFactoryUtils.createDefaultThreadPool("custom-shutdown-hook-rpc-pool");
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();
    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }
    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            threadPool.shutdown();
        }));
    }
}
