package com.zhi.registry.zk.util;

import com.zhi.enums.RpcConfigPropertiesEnum;
import com.zhi.exception.RpcException;
import com.zhi.utils.file.PropertiesFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    //CuratorFramework还是尽量在CuratorHelper这个使用到了的这个类中进行初始化吧，要不然放在方法参数的话，
    //外部类每次调用这个工具类都要传一次显然是不合理的...  ->  从该类的static代码块new对象到通过单例工厂
    private static CuratorFramework zkClient;
    private static String defaultZookeeperAddress = "192.168.43.130:2181";

    private CuratorUtils() {
    }

    /**
     * 创建临时节点
     * 临时节点驻存在ZooKeeper中，当连接和session断掉时被删除。
     * v[3.0]进行了优化，先检验节点是否已经存在，如果已经存在就没有必要在创建了~
     * v[3.1]创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     * @param path 节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点已经存在，节点为:[{}]", path);
            } else {
                //eg: /my-rpc/com.zhi.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
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
     * @param rpcServiceName 服务对象接口名 eg:com.zhi.HelloService
     * @return 指定字节下的所有子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 清除注册中心的数据
     */
    public static void clearRegistry(CuratorFramework zkClient) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
            log.info("服务端（Provider）所有注册的服务都被清空:[{}]", REGISTERED_PATH_SET.toString());
        });
    }
    //进行了优化，使用ExponentialBackoffRetry，代替RetryNTimes
    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigPropertiesEnum.RPC_CONFIG_PATH.getPropertyValue());
        if (properties != null) {
            defaultZookeeperAddress = properties.getProperty(RpcConfigPropertiesEnum.ZK_ADDRESS.getPropertyValue());
        }
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略。重试3次，并且会增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(defaultZookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * 注册监听指定节点
     * 其实就是每当有新服务生产者注册进来就更新服务地址注册表...
     * @param rpcServiceName 服务对象接口名 eg:com.zhi.HelloService
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }
}