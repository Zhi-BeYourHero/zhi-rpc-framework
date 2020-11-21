package com.zhi.remoting.transport.netty.client;

import com.zhi.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 用于获取Channel对象
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:28
 */
@Slf4j
public class ChannelProvider {
    private final Map<String, Channel> channelMap;
    private final NettyClient nettyClient;
    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 如果有的话，判断连接是否可用，可用的话就直接获取
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        //否则，就重新连接
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channelMap.put(inetSocketAddress.toString(), channel);
        return channel;
    }
    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }
}