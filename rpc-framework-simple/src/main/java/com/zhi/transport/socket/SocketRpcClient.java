package com.zhi.transport.socket;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.enumeration.RpcErrorMessageEnum;
import com.zhi.enumeration.RpcResponseCode;
import com.zhi.exception.RpcException;
import com.zhi.transport.RpcClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:26
 */
@AllArgsConstructor
public class SocketRpcClient implements RpcClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcClient.class);
    //V[2.0]改了一下组织结构，将host和port从方法参数变成类成员变量，原因：
    private String host;
    private int port;

    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //1. 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //2.通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            //3.通过输入流获取服务器响应的信息
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            if (rpcResponse == null) {
                LOGGER.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
                LOGGER.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
            }
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("occur exception:", e);
        }
        return null;
    }
}
