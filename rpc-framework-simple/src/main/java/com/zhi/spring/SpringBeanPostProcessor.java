package com.zhi.spring;

import com.zhi.annotation.RpcReference;
import com.zhi.annotation.RpcService;
import com.zhi.enums.RpcConfigPropertiesEnum;
import com.zhi.registry.IRegisterCenter4Invoker;
import com.zhi.registry.IRegisterCenter4Provider;
import com.zhi.registry.zk.util.RegisterCenter;
import com.zhi.remoting.model.InvokerService;
import com.zhi.remoting.model.ProviderService;
import com.zhi.revoker.NettyChannelPoolFactory;
import com.zhi.revoker.RevokerProxyBeanFactory;
import com.zhi.utils.file.PropertiesFileUtils;
import com.zhi.utils.ip.IPUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 15:17
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();

    /**
     * 将@RpcService注解标注的类进行注册
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 生成服务方发布的服务信息
            List<ProviderService> providerServiceList = new ArrayList<>();
            Method[] methods = bean.getClass().getDeclaredMethods();
            Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigPropertiesEnum.RPC_CONFIG_PATH.getPropertyValue());
            String serverPort = properties.getProperty(RpcConfigPropertiesEnum.PORT.getPropertyValue());
            // 为每一个方法都生成对应的ProviderService...
            for (Method method : methods) {
                ProviderService providerService = new ProviderService();
                providerService.setServiceItf(bean.getClass().getInterfaces()[0]);
                providerService.setServiceObject(bean);
                //获取本机IP
                providerService.setServerIp(IPUtil.localIp());
                providerService.setServerPort(Integer.parseInt(serverPort));
                providerService.setTimeout(rpcService.timeout());
                providerService.setServiceMethod(method);
                providerService.setWeight(rpcService.weight());
                providerService.setWorkerThreads(rpcService.workThreads());
                providerService.setAppKey(rpcService.appKey());
                providerService.setGroupName(rpcService.groupName());
                providerService.setVersion(rpcService.version());
                log.info("尝试获取：版本" + beanName + rpcService.group() + rpcService.version());
                providerService.setGroup(rpcService.group());
                providerServiceList.add(providerService);
            }
            // 初始化Zookeeper的链接并且将服务信息`providerServiceList`注册到ZK结点上并且监听变化
            registerCenter4Provider.registerProvider(providerServiceList);
        }
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                //获取服务注册中心
                IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
                //初始化服务提供者列表到本地缓存,指定分组
                registerCenter4Consumer.initProviderMap(rpcReference.remoteAppKey(), rpcReference.groupName());

                //初始化Netty Channel(从Zookeeper配置中心拉取到多少服务，就建立多少netty-client，维持在ip地址对应ArrayBlockingQueue<Channel>的Map中)
                // `providerMap`是从Zookeeper中拉取的
                Map<String, List<ProviderService>> providerMap = registerCenter4Consumer.getServiceMetaDataMap4Consume();
                if (MapUtils.isEmpty(providerMap)) {
                    throw new RuntimeException("service provider list is empty.");
                }
                NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(providerMap);

                //获取服务提供者代理对象(是JDK的动态代理)
                RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.singleton(declaredField.getType(), rpcReference.timeout(), rpcReference.clusterStrategy(), rpcReference.failMode());
                Object clientProxy = proxyFactory.getProxy();
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                //将消费者信息注册到注册中心(让注册中心知道有多少服务消费者)
                InvokerService invoker = new InvokerService();
                invoker.setServiceItf(declaredField.getType());
                invoker.setRemoteAppKey(rpcReference.remoteAppKey());
                invoker.setGroupName(rpcReference.groupName());

                // 将消费者信息注册到注册中心
                registerCenter4Consumer.registerInvoker(invoker);
            }

        }
        return bean;
    }
}