import com.zhi.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description  Server: Automatic registration service via @RpcService annotation
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
@ComponentScan("com.zhi")
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();
    }
}