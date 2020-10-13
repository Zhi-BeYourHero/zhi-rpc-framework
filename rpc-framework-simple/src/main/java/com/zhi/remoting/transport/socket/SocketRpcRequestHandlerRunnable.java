package com.zhi.remoting.transport.socket;

import com.zhi.remoting.dto.RpcRequest;
import com.zhi.remoting.dto.RpcResponse;
import com.zhi.handler.RpcRequestHandler;
import com.zhi.utils.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Description 服务器端接收到socket并进行处理
 * @Author WenZhiLuo
 * @Date 2020-10-10 15:26
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable {
    private Socket socket;
    //TODO 修改了RpcRequestHandler和ServiceRegistry的创造方式，不再从方法参数传过来，而是由静态代码块加载,那么问题来了，为什么要这样设置？
    private RpcRequestHandler rpcRequestHandler;
    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: {}", Thread.currentThread().getName());
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception:", e);
        }
    }
}