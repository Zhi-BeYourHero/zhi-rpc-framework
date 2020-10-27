import com.zhi.HelloServiceImpl;
import com.zhi.api.HelloService;
import com.zhi.provider.ServiceProvider;
import com.zhi.provider.ServiceProviderImpl;
import com.zhi.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
@ComponentScan("com.zhi")
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        serviceProvider.publishService(helloService);
    }
}