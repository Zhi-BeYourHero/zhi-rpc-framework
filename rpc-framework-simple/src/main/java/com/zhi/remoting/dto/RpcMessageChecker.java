package com.zhi.remoting.dto;

import com.zhi.enumeration.RpcErrorMessage;
import com.zhi.enumeration.RpcResponseCode;
import com.zhi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 校验 RpcRequest 和 RpcRequest
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
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}