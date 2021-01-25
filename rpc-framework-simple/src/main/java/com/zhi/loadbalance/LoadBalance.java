package com.zhi.loadbalance;

import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:10
 */
public interface LoadBalance {
    /**
     * 在已有服务提供地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @return 目标服务地址
     */
    <T> T selectServiceAddress(List<T> serviceAddresses);
}