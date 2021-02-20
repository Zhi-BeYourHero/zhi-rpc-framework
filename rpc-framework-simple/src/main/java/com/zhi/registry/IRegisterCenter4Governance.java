package com.zhi.registry;

import com.zhi.remoting.model.InvokerService;
import com.zhi.remoting.model.ProviderService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-18 21:05
 */
public interface IRegisterCenter4Governance {
    /**
     * 获取服务提供者列表与服务消费者列表
     *
     * @param serviceName
     * @param appKey
     * @return
     */
    Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey);

}