package com.zhi.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 12:13
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}