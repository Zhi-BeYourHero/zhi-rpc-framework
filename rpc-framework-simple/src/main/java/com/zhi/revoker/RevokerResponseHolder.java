package com.zhi.revoker;

import com.google.common.collect.Maps;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.remoting.model.RpcResponseWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description 储存同步调用结果，并且将结果维持在一个Map中，根据配置的服务调用超时时间判断结果是否超时。
 * @Author WenZhiLuo
 * @Date 2021-02-19 21:30
 */
public class RevokerResponseHolder {

    /** 服务返回结果Map，key是一次服务的TraceId/UUID */
    private static final Map<String, RpcResponseWrapper> RESPONSE_MAP = Maps.newConcurrentMap();

    /** 单线程池，清理超时过期的返回结果 */
    private static final ExecutorService REMOVE_EXPIRE_KEY_EXECUTOR = Executors.newSingleThreadExecutor();

    static {
        // 删除超时未获取到结果的key,防止内存泄露
        REMOVE_EXPIRE_KEY_EXECUTOR.execute(() -> {
            // 每10ms检验一次是否过期
            while (true) {
                try {
                    for (Map.Entry<String, RpcResponseWrapper> entry : RESPONSE_MAP.entrySet()) {
                        boolean isExpire = entry.getValue().isExpire();
                        if (isExpire) {
                            RESPONSE_MAP.remove(entry.getKey());
                        }
                        // 10毫秒一次
                        Thread.sleep(10);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Step1：（RPC调用用户线程）初始化返回结果容器,requestId唯一标识本次调用
     * 这个方法是被`跑RPC远程调用Task`的线程在执行call()方法中代码的第一句时候初始化的，所以后续不用担心NPE。
     * @param requestId
     */
    public static void initResponseData(String requestId) {
        RESPONSE_MAP.put(requestId, RpcResponseWrapper.of());
    }

    /**
     * Step2：（RPC调用用户线程）将Netty调用异步返回结果放入阻塞队列。
     * 这个方法是被`NIO线程`运行的`pipeline`中的`handler`所调用的，将服务调用结果放入结果集Map中。
     * @param response
     */
    public static void putResultValue(RpcResponse<Object> response) {
        long currentTime = System.currentTimeMillis();
        RpcResponseWrapper responseWrapper = RESPONSE_MAP.get(response.getRequestId());
        responseWrapper.setResponseTime(currentTime);
        // !!!add背后调用offer、如果队列满了，则抛出异常IllegalStateException("Queue full"); 而不是put会阻塞直到有可用空间
        responseWrapper.getResponseQueue().add(response);
        RESPONSE_MAP.put(response.getRequestId(), responseWrapper);
    }

    /**
     * Step3：（RPC调用用户线程）从阻塞队列中获取Netty异步返回的结果值。
     * 这个方法是被`跑RPC远程调用Task`的线程调用的。
     * 特别注意：超时时间是在获取阻塞队列中结果集时候的最长等待时间。
     * @param requestId
     * @param timeout
     * @return
     */
    public static RpcResponse<Object> getValue(String requestId, long timeout) {
        RpcResponseWrapper responseWrapper = RESPONSE_MAP.get(requestId);
        // 特别注意：必须保证服务调用前先预创建一个PlaceHolder在Map中，否则会有可能NPE!!!
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 无论获取成功与否，都清除掉这个结果集
            RESPONSE_MAP.remove(requestId);
        }
    }
}