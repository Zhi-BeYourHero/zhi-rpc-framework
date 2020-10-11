package com.zhi.transport.socket;

import com.zhi.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @Description v[2.0]进行了优化，RpcRequestHandler和ServiceRegistry由SocketRpcRequestHandlerRunnable创建，Server端更优雅
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:10
 */
public class SocketRpcServer {
    private ExecutorService threadPool;
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcServer.class);

    public SocketRpcServer() {
        this.threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-server-rpc-pool");
    }

    /**
     * 服务器端：
     * 1. 创建 ServerSocket 对象并且绑定地址（ip）和端口号(port)：server.bind(new InetSocketAddress(host, port))
     * 2. 通过 accept()方法监听客户端请求
     * 3. 连接建立后，通过输入流读取客户端发送的请求信息
     * 4. 通过输出流向客户端发送响应信息
     * 5. 关闭相关资源
     * @param port 端口
     */
    public void start(int port) {
        //1.创建 ServerSocket 对象并且绑定一个端口
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("server starts...");
            Socket socket;
            //2.通过 accept()方法监听客户端请求
            while ((socket = serverSocket.accept()) != null) {
                LOGGER.info("client connected...");
                //将socket交给线程池进行处理，
                //线程池还可以让线程的创建和回收成本相对较低，并且我们可以指定线程池的可创建线程的最大数量，这样就不会导致线程创建过多，机器资源被不合理消耗。
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            LOGGER.error("Occur IOException: ", e);
        }
    }
}