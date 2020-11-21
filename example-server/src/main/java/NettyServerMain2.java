import com.zhi.annotation.RpcScan;
import com.zhi.serviceimpl.HelloServiceImpl;
import com.zhi.api.HelloService;
import com.zhi.entity.RpcServiceProperties;
import com.zhi.provider.ServiceProvider;
import com.zhi.provider.ServiceProviderImpl;
import com.zhi.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description Server: Manually register the service
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
@RpcScan(basePackage = {"com.zhi.serviceimpl"})
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain2.class);
        NettyServer nettyServer = new NettyServer();
        System.out.println("我我问问我我我我我我我");
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test3").version("version3").build();
        serviceProvider.publishService(helloService, rpcServiceProperties);
        nettyServer.start();
    }
}