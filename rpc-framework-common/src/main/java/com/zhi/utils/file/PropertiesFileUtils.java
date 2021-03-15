package com.zhi.utils.file;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 14:35
 */
@Slf4j
public final class PropertiesFileUtils {

    private static final String PROPERTY_CLASSPATH = "rpc.properties";
    private static final Properties PROPERTIES = readPropertiesFile(PROPERTY_CLASSPATH);

    //ZK服务地址
    private static String zkService = "";
    //ZK session超时时间
    private static int zkSessionTimeout;
    //ZK connection超时时间
    private static int zkConnectionTimeout;
    //每个服务端提供者的Netty的连接数
    private static int channelConnectSize;

    /**
     * 初始化
     */
    static {
        try {
            zkService = PROPERTIES.getProperty("zk_service");
            zkSessionTimeout = Integer.parseInt(PROPERTIES.getProperty("zk_sessionTimeout", "500"));
            zkConnectionTimeout = Integer.parseInt(PROPERTIES.getProperty("zk_connectionTimeout", "500"));
            channelConnectSize = Integer.parseInt(PROPERTIES.getProperty("channel_connect_size", "10"));
        } catch (Throwable t) {
            log.warn("load ares_remoting's properties file failed.", t);
            throw new RuntimeException(t);
        }
    }
    private PropertiesFileUtils() {
    }
    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
            new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException ioException) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }

    public static String getZkService() {
        return zkService;
    }

    public static int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public static int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public static int getChannelConnectSize() {
        return channelConnectSize;
    }
}