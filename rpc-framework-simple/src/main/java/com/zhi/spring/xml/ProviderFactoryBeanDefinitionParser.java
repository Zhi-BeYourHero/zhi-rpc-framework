package com.zhi.spring.xml;

import com.zhi.provider.ProviderFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-19 23:26
 */
@Slf4j
public class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class getBeanClass(Element element) {
        return ProviderFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {

        try {
            String serviceItf = element.getAttribute("interface");
            String timeOut = element.getAttribute("timeout");
            String serverPort = element.getAttribute("serverPort");
            String ref = element.getAttribute("ref");
            String weight = element.getAttribute("weight");
            String workerThreads = element.getAttribute("workerThreads");
            String appKey = element.getAttribute("appKey");
            String groupName = element.getAttribute("groupName");
            String group = element.getAttribute("group");
            String version = element.getAttribute("version");

            bean.addPropertyValue("serverPort", Integer.parseInt(serverPort));
            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));
            bean.addPropertyValue("serviceItf", Class.forName(serviceItf));
            bean.addPropertyReference("serviceObject", ref);
            bean.addPropertyValue("appKey", appKey);

            if (NumberUtils.isNumber(weight)) {
                bean.addPropertyValue("weight", Integer.parseInt(weight));
            }
            if (NumberUtils.isNumber(workerThreads)) {
                bean.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
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
            log.error("ProviderFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }
    }
}