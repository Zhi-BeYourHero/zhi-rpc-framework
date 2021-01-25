package com.zhi.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.zhi.exception.RpcException;
import com.zhi.registry.ServiceRegistry;
import com.zhi.registry.nacos.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-01-25 9:46
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.registerService(rpcServiceName, inetSocketAddress);
        } catch (NacosException e) {
            log.error("注册服务时有错误发生:", e);
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }
}
