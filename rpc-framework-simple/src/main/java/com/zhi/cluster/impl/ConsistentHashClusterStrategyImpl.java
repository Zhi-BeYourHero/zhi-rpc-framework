package com.zhi.cluster.impl;

import com.zhi.cluster.ClusterStrategy;
import com.zhi.remoting.model.ProviderService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 软负载一致性哈希算法实现 //TODO 应该有点小问题，回头整的差不多再看看
 * refer to dubbo consistent hash load balance: https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 * @Author WenZhiLuo
 * @Date 2021-02-19 18:26
 */
public class ConsistentHashClusterStrategyImpl implements ClusterStrategy {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int identityHashCode = System.identityHashCode(providerServices);

        String rpcServiceName = providerServices.get(0).getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(providerServices, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName);
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, ProviderService> virtualInvokers;

        private final int identityHashCode;

        ConsistentHashSelector(List<ProviderService> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (ProviderService invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 根据IP进行一致性hash
                    byte[] digest = md5(invoker.getServerIp() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public ProviderService select(String rpcServiceName) {
            byte[] digest = md5(rpcServiceName);
            return selectForKey(hash(digest, 0));
        }

        public ProviderService selectForKey(long hashCode) {
            Map.Entry<Long, ProviderService> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}