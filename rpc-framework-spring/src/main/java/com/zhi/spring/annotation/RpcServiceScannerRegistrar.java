package com.zhi.spring.annotation;

import com.zhi.spring.rpcservice.RpcServiceScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

/**
 * @Description
 * ResourceLoaderAware 获取资源加载器,可以获得外部资源文件
 * ImportBeanDefinitionRegistrar动态注册bean,
 * ImportBeanDefinitionRegistrar类只能通过其他类@Import的方式来加载，通常是启动类或配置类。
 * 这里是通过RpcServiceScan来@Import
 * @Author WenZhiLuo
 * @Date 2020-10-23 22:03
 */
public class RpcServiceScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(RpcServiceScan.class.getName())
        );
        RpcServiceScanner scanner = new RpcServiceScanner(registry);
        String value = annotationAttributes.getString("value");
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        //所有的接口全部注入
        scanner.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                return true;
            }
        });
        scanner.doScan(value);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
