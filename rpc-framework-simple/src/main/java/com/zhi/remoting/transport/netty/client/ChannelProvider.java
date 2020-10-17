package com.zhi.remoting.transport.netty.client;

import com.zhi.enumeration.RpcErrorMessageEnum;
import com.zhi.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Description 用于获取Channel对象
 * @Author WenZhiLuo
 * @Date 2020-10-12 9:28
 */
@Slf4j
public class ChannelProvider {
    private static final Bootstrap BOOTSTRAP = NettyClient.getBootstrap();
    private static Channel channel = null;
    /**
     * 最多重试次数
     */
    private static final int MAX_RETRY_COUNT = 5;
    public static Channel get(InetSocketAddress inetSocketAddress) throws InterruptedException {
        //通过countDownLatch保证有序执行
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connect(BOOTSTRAP, inetSocketAddress, countDownLatch);
        countDownLatch.await();
        return channel;
    }

    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }
    /**
     * 带有重试机制的客户端连接方法
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                countDownLatch.countDown();
                throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
            }
            //第几次重连
            int order = (MAX_RETRY_COUNT - retry) + 1;
            //本次重连间隔时间
            int delay = 1 << order;
            log.error("{}: 连接失败，第 {} 次重连……", new Date(), order);
            bootstrap.config().group().schedule(() -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch),
                    delay, TimeUnit.SECONDS);
        });
    }
}