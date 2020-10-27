package com.zhi.spring;

import com.zhi.annotation.RpcService;
import com.zhi.entity.RpcServiceProperties;
import com.zhi.factory.SingletonFactory;
import com.zhi.provider.ServiceProvider;
import com.zhi.provider.ServiceProviderImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 15:17
 */
@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    public SpringBeanPostProcessor() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            //build RpcServiceProperties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group(rpcService.group())
                    .version(rpcService.version()).build();
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }
}