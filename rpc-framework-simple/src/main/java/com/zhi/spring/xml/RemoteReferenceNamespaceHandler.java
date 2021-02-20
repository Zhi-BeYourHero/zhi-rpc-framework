package com.zhi.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Description 服务引入自定义标签
 * @Author WenZhiLuo
 * @Date 2021-02-19 23:17
 */
public class RemoteReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new RevokerFactoryBeanDefinitionParser());
    }
}
