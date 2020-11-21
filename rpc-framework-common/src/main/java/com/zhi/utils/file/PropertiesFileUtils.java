package com.zhi.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 14:35
 */
@Slf4j
public final class PropertiesFileUtils {
    private PropertiesFileUtils() {
    }
    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (FileInputStream fileInputStream = new FileInputStream(rpcConfigPath)) {
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException ioException) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}
