package com.zhi.remoting.transport.socket;

import com.zhi.entity.RpcServiceProperties;
import com.zhi.extension.ExtensionLoader;
import com.zhi.remoting.dto.RpcRequest;
import com.zhi.exception.RpcException;
import com.zhi.registry.ServiceDiscovery;
import com.zhi.registry.zk.ZkServiceDiscovery;
import com.zhi.remoting.transport.ClientTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SocketRpcClient implements ClientTransport {
    //TODO V[2.0]改了一下组织结构，将host和port从方法参数变成类成员变量，原因：
    /*
    * 从原来的手动指定host和port转为通过服务注册中心去获取主机地址
    * */
    private final ServiceDiscovery serviceDiscovery;
    public SocketRpcClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build rpc service name by ppcRequest
        String rpcServiceName = RpcServiceProperties.builder().serviceName(rpcRequest.getInterfaceName())
                .group(rpcRequest.getGroup()).version(rpcRequest.getVersion()).build().toRpcServiceName();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        //1. 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //2.通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            //3.通过输入流获取服务器响应的信息
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }
    }
}
