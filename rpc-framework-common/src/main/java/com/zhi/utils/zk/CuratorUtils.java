package com.zhi.utils.zk;

import com.zhi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-12 22:03
 */
@Slf4j
public final class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    //从 5 改成 3 ，布吉岛为啥
    private static final int MAX_RETRIES = 3;
    private static final String CONNECT_STRING = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    //CuratorFramework还是尽量在CuratorHelper这个使用到了的这个类中进行初始化吧，要不然放在方法参数的话，
    //外部类每次调用这个工具类都要传一次显然是不合理的...
    private static final CuratorFramework ZK_CLIENT;
    static {
        ZK_CLIENT = getZkClient();
    }
    private CuratorUtils() {
    }

    /**
     * 创建临时节点
     * 临时节点驻存在ZooKeeper中，当连接和session断掉时被删除。
     * v[3.0]进行了优化，先检验节点是否已经存在，如果已经存在就没有必要在创建了~
     * v[3.1]创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     * @param path 节点路径
     */
    public static void createPersistentNode(String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || ZK_CLIENT.checkExists().forPath(path) != null) {
                log.info("节点已经存在，节点为:[{}]", path);
            } else {
                //eg: /my-rpc/com.zhi.HelloService/127.0.0.1:9999
                ZK_CLIENT.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取某个字节下的子节点，也就是获取所有提供服务的生产者的地址
     * v[3.0]进行了优化，result不用一定要指定为null，后面如果报错直接throw exception
     * @param serviceName 服务对象接口名 eg:com.zhi.HelloService
     * @return 指定字节下的所有子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(serviceName)) {
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = ZK_CLIENT.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, result);
            registerWatcher(serviceName);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 清除注册中心的数据
     */
    public static void clearRegistry() {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                ZK_CLIENT.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
            log.info("服务端（Provider）所有注册的服务都被清空:[{}]", REGISTERED_PATH_SET.toString());
        });
    }
    //进行了优化，使用ExponentialBackoffRetry，代替RetryNTimes
    public static CuratorFramework getZkClient() {
        // 重试策略。重试3次，并且会增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    /**
     * 注册监听指定节点
     *
     * @param serviceName 服务对象接口名 eg:com.zhi.HelloService
     */
    private static void registerWatcher(String serviceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(ZK_CLIENT, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }
}