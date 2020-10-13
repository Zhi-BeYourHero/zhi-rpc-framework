package com.zhi.transport.socket;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.exception.RpcException;
import com.zhi.registry.ServiceRegistry;
import com.zhi.registry.ZkServiceRegistry;
import com.zhi.transport.ClientTransport;
import com.zhi.utils.checker.RpcMessageChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:26
 */
@AllArgsConstructor
public class SocketRpcClient implements ClientTransport {
    public static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcClient.class);
    //TODO V[2.0]改了一下组织结构，将host和port从方法参数变成类成员变量，原因：
    /*
    * 从原来的手动指定host和port转为通过服务注册中心去获取主机地址
    * */
    private final ServiceRegistry serviceRegistry;
    public SocketRpcClient() {
        this.serviceRegistry = new ZkServiceRegistry();
    }
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());
        //1. 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //2.通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            //3.通过输入流获取服务器响应的信息
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            //校验 RpcResponse 和 RpcRequest
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("occur exception when send sendRpcRequest");
            throw new RpcException("调用服务失败:", e);
        }
    }
}
