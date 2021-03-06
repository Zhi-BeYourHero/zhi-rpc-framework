package com.zhi.remoting.transport.socket;

import com.zhi.config.CustomShutdownHook;
import com.zhi.factory.SingletonFactory;
import com.zhi.provider.ServiceProviderImpl;
import com.zhi.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @Description v[2.0]进行了优化，RpcRequestHandler和ServiceRegistry由SocketRpcRequestHandlerRunnable创建，Server端更优雅
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:10
 */
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    /*
    * 添加主机host和port,服务提供者和服务注册中心
    * */
    private final String host;
    private final int port;
    public SocketRpcServer(String host, int port) {
        this.threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        this.host = host;
        this.port = port;
        SingletonFactory.getInstance(ServiceProviderImpl.class);
    }
    /**
     * 服务器端：
     * 1. 创建 ServerSocket 对象并且绑定地址（ip）和端口号(port)：server.bind(new InetSocketAddress(host, port))
     * 2. 通过 accept()方法监听客户端请求
     * 3. 连接建立后，通过输入流读取客户端发送的请求信息
     * 4. 通过输出流向客户端发送响应信息
     * 5. 关闭相关资源
     */
    public void start() {
        //1.创建 ServerSocket 对象并且绑定一个端口
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            //2.通过 accept()方法监听客户端请求
            while ((socket = serverSocket.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                //将socket交给线程池进行处理，
                //线程池还可以让线程的创建和回收成本相对较低，并且我们可以指定线程池的可创建线程的最大数量，这样就不会导致线程创建过多，机器资源被不合理消耗。
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("Occur IOException: ", e);
        }
    }
}