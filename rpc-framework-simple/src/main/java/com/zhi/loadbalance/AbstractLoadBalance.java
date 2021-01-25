package com.zhi.loadbalance;

import java.util.List;

/**
 * @Description Abstract class for a load balancing policy
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:10
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public <T> T selectServiceAddress(List<T> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    protected abstract <T> T doSelect(List<T> serviceAddresses);
}