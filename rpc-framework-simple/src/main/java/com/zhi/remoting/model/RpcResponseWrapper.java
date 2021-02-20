package com.zhi.remoting.model;

import com.zhi.remoting.dto.RpcResponse;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @Description Netty异步调用返回结果包装类。
 * 特别注意，一次服务调用有一个确切的结果，被包装成`RpcResponseWrapper`。
 * @Author WenZhiLuo
 * @Date 2021-02-19 21:33
 */
public class RpcResponseWrapper {
    /** RPC调用结果的返回时间 */
    private long responseTime;

    /** 容量只有1的阻塞队列 */
    private BlockingQueue<RpcResponse<Object>> responseQueue = new ArrayBlockingQueue<>(1);

    /**
     * 静态工厂方式生成对象。
     * @return
     */
    public static RpcResponseWrapper of() {
        return new RpcResponseWrapper();
    }

    /**
     * 计算该返回结果是否已经过期
     *
     * @return
     */
    public boolean isExpire() {
        RpcResponse<Object> response = responseQueue.peek();
        if (response == null) {
            return false;
        }
        //获取超时时间
        long timeout = response.getInvokeTimeout();
        return (System.currentTimeMillis() - responseTime) > timeout;
    }

    public BlockingQueue<RpcResponse<Object>> getResponseQueue() {
        return responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
