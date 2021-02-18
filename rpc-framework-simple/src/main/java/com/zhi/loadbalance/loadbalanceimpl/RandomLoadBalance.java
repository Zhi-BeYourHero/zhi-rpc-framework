package com.zhi.loadbalance.loadbalanceimpl;

import com.zhi.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:13
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected <T> T doSelect(List<T> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}