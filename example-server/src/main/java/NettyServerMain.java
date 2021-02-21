import com.zhi.annotation.RpcScan;
import com.zhi.enums.RpcConfigPropertiesEnum;
import com.zhi.remoting.transport.netty.server.NettyServer;
import com.zhi.utils.file.PropertiesFileUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Properties;

/**
 * @Description Server: Automatic registration service via @RpcService annotation
 * Server: Manually register the service
 * @Author WenZhiLuo
 * @Date 2020-10-11 15:32
 */
@RpcScan(basePackage = {"com.zhi.serviceimpl"})
public class NettyServerMain {
    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
        new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = new NettyServer();
//        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
//                .group("test3").version("version3").build();
//        nettyServer.registerService(helloService, rpcServiceProperties);
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigPropertiesEnum.RPC_CONFIG_PATH.getPropertyValue());
        String serverPort = properties.getProperty(RpcConfigPropertiesEnum.PORT.getPropertyValue());
        nettyServer.start(Integer.parseInt(serverPort));
    }
}