package com.zhi.utils.checker;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.enumeration.RpcErrorMessageEnum;
import com.zhi.enumeration.RpcResponseCode;
import com.zhi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 19:01
 */
@Slf4j
public final class RpcMessageChecker {
    private static final String INTERFACE_NAME = "interfaceName";
    private RpcMessageChecker() {
    }
    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            log.error("服务调用失败,rpcResponse 为 null，serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            log.error("服务调用失败，requestId不同,rpcRequest 和 rpcResponse 对应不上,serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            log.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}