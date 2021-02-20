package com.zhi.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 23:26
 */
public class RemoteServiceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
    }
}
