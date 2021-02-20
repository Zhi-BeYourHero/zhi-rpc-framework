package com.zhi.revoker;

import com.google.common.collect.Maps;
import com.zhi.remoting.model.ProviderService;
import com.zhi.remoting.transport.netty.client.NettyClient;
import com.zhi.utils.file.PropertiesFileUtils;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description Netty通道池工厂，队列使用`ArrayBlockingQueue`来存放生产者。
 * @Author WenZhiLuo
 * @Date 2021-02-19 11:05
 */
@Slf4j
public final class NettyChannelPoolFactory {
    private static final NettyChannelPoolFactory CHANNEL_POOL_FACTORY = new NettyChannelPoolFactory();

    //Key为服务提供者地址,value为Netty Channel阻塞队列(核心数据结构)
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> CHANNEL_POOL_MAP = new ConcurrentHashMap<>();
    //初始化Netty Channel阻塞队列的长度,该值为可配置信息
    private static final int CHANNEL_CONNECT_SIZE = PropertiesFileUtils.getChannelConnectSize();
    //服务提供者列表
    private List<ProviderService> serviceMetaDataList = new ArrayList<>();

    private NettyChannelPoolFactory() {
    }


    /**
     * 初始化Netty channel 连接队列Map
     *
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap) {
        //将服务提供者信息存入serviceMetaDataList列表
        Collection<List<ProviderService>> collectionServiceMetaDataList = providerMap.values();
        for (List<ProviderService> serviceMetaDataModels : collectionServiceMetaDataList) {
            if (CollectionUtils.isEmpty(serviceMetaDataModels)) {
                continue;
            }
            serviceMetaDataList.addAll(serviceMetaDataModels);
        }

        //获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = new HashSet<>();
        for (ProviderService serviceMetaData : serviceMetaDataList) {
            String serviceIp = serviceMetaData.getServerIp();
            int servicePort = serviceMetaData.getServerPort();

            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        }
        //根据服务提供者地址列表初始化Channel阻塞队列,并以地址为Key,地址对应的Channel阻塞队列为value,存入channelPoolMap
        for (InetSocketAddress socketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < CHANNEL_CONNECT_SIZE) {
                    // netty的通道
                    Channel channel = null;
                    while (channel == null) {
                        //若channel不存在,则注册新的Netty Channel
                        channel = registerChannel(socketAddress);
                    }
                    //计数器,初始化的时候存入阻塞队列的Netty Channel个数不超过channelConnectSize
                    realChannelConnectSize++;
                    //将新注册的Netty Channel存入阻塞队列channelArrayBlockingQueue
                    // 并将阻塞队列channelArrayBlockingQueue作为value存入channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = CHANNEL_POOL_MAP.get(socketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<>(CHANNEL_CONNECT_SIZE);
                        CHANNEL_POOL_MAP.put(socketAddress, channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     *
     * @param socketAddress
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return CHANNEL_POOL_MAP.get(socketAddress);
    }


    /**
     * Channel使用完毕之后,回收到阻塞队列arrayBlockingQueue
     *
     * @param arrayBlockingQueue
     * @param channel
     * @param inetSocketAddress
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress inetSocketAddress) {
        if (arrayBlockingQueue == null) {
            return;
        }

        //回收之前先检查channel是否可用,不可用的话,重新注册一个,放入阻塞队列
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (newChannel == null) {
                log.debug("---------register new Channel-------------");
                newChannel = registerChannel(inetSocketAddress);
            }
            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }


    /**
     * 为服务提供者地址socketAddress注册新的Channel，
     * 如果从Zookeeper中心获取服务者列表有200个，而池子中允许的连接数最多是100个，则要初始化100个连接到服务端的netty-client通道。
     *
     * @param socketAddress
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        NettyClient nettyClient = new NettyClient();
        return nettyClient.doConnect(socketAddress);
    }


    public static NettyChannelPoolFactory channelPoolFactoryInstance() {
        return CHANNEL_POOL_FACTORY;
    }
}
