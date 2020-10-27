package com.zhi.exception;

import com.zhi.enumeration.RpcErrorMessage;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 13:55
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessage rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}