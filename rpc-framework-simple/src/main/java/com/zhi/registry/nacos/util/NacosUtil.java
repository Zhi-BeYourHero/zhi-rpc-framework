package com.zhi.registry.nacos.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description 管理Nacos连接等工具类
 * @Author WenZhiLuo
 * @Date 2021-01-25 9:38
 */
@Slf4j
public class NacosUtil {

    private static final NamingService NAMING_SERVICE;
    private static final Set<String> SERVICE_NAMES = new HashSet<>();
    private static InetSocketAddress address;
    private static final String SERVER_ADDR = "127.0.0.1:8848";

    static {
        NAMING_SERVICE = getNacosNamingService();
    }

    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        NAMING_SERVICE.registerInstance(serviceName, address.getHostName(), address.getPort());
        NacosUtil.address = address;
        SERVICE_NAMES.add(serviceName);
    }

    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return NAMING_SERVICE.getAllInstances(serviceName);
    }

    public static void clearRegistry() {
        if (!SERVICE_NAMES.isEmpty() && address != null) {
            String host = address.getHostName();
            int port = address.getPort();
            for (String serviceName : SERVICE_NAMES) {
                try {
                    NAMING_SERVICE.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    throw new RpcException(e.getMessage(), e.getCause());
                }
            }
        }
    }
}
