package com.zhi.dto;

import com.zhi.enumeration.RpcResponseCode;
import lombok.*;

import java.io.Serializable;

/**
 * @Description v[2.0]从原来的导入@Data改为指定需要的，估计是避免不必要的方法吧
 * @Author WenZhiLuo
 * @Date 2020-10-10 13:40
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * 响应码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setRequestId(requestId);
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return response;
    }
}