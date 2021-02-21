package com.zhi.spring.xml;

import com.zhi.revoker.RevokerFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 23:23
 */
@Slf4j
public class RevokerFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return RevokerFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        // 简单属性的解析(不需要RootBeanDefinition)
        try {
            String timeOut = element.getAttribute("timeout");
            String targetInterface = element.getAttribute("interface");
            String clusterStrategy = element.getAttribute("clusterStrategy");
            String remoteAppKey = element.getAttribute("remoteAppKey");
            String groupName = element.getAttribute("groupName");
            String failMode = element.getAttribute("failMode");
            String group = element.getAttribute("group");
            String version = element.getAttribute("version");
            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));
            bean.addPropertyValue("targetInterface", Class.forName(targetInterface));
            bean.addPropertyValue("remoteAppKey", remoteAppKey);
            if (StringUtils.isNotBlank(failMode)) {
                bean.addPropertyValue("failMode", failMode);
            }
            if (StringUtils.isNotBlank(clusterStrategy)) {
                bean.addPropertyValue("clusterStrategy", clusterStrategy);
            }
            if (StringUtils.isNotBlank(groupName)) {
                bean.addPropertyValue("groupName", groupName);
            }
            if (StringUtils.isNotBlank(group)) {
                bean.addPropertyValue("group", group);
            }
            if (StringUtils.isNotBlank(version)) {
                bean.addPropertyValue("version", version);
            }
        } catch (Exception e) {
            log.error("RevokerFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }
    }
}