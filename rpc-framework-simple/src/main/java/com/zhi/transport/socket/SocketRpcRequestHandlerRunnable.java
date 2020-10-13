package com.zhi.transport.socket;

import com.zhi.dto.RpcRequest;
import com.zhi.dto.RpcResponse;
import com.zhi.handler.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Description 服务器端接收到socket并进行处理
 * @Author WenZhiLuo
 * @Date 2020-10-10 15:26
 */
public class SocketRpcRequestHandlerRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);
    private Socket socket;
    //TODO 修改了RpcRequestHandler和ServiceRegistry的创造方式，不再从方法参数传过来，而是由静态代码块加载,那么问题来了，为什么要这样设置？
    private static final RpcRequestHandler RPC_REQUEST_HANDLER;

    static {
        RPC_REQUEST_HANDLER = new RpcRequestHandler();
    }

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        LOGGER.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = RPC_REQUEST_HANDLER.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("occur exception:", e);
        }
    }
}