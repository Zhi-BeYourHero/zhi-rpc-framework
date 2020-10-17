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
public final class ChannelProvider {
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static NettyClient nettyClient;
    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }
    private ChannelProvider() {
    }
    public static Channel get(InetSocketAddress inetSocketAddress) {
        //通过countDownLatch保证有序执行
        String key = inetSocketAddress.toString();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        //否则，就重新连接
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channels.put(inetSocketAddress.toString(), channel);
        return channel;
    }
}