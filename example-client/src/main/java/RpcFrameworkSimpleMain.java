import com.zhi.Hello;
import com.zhi.HelloService;
import com.zhi.RpcClientProxy;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-10 11:51
 */
public class RpcFrameworkSimpleMain {
    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9999);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("21345", "786534"));
        System.out.println(hello);
    }
}
