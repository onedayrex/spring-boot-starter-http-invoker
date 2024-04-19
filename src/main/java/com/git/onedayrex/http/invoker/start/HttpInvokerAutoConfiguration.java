package com.git.onedayrex.http.invoker.start;

import com.git.onedayrex.http.invoker.start.client.RemoteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


public class HttpInvokerAutoConfiguration implements BeanDefinitionRegistryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(HttpInvokerAutoConfiguration.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        String[] beanDefinitionNames = beanDefinitionRegistry.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitionName);
            if(beanDefinition instanceof RootBeanDefinition){
                continue;
            }
            try {
                Class<?> beanClazz = ClassUtils.forName(beanDefinition.getBeanClassName(), null);
                ReflectionUtils.doWithLocalFields(beanClazz,field -> {
                    RemoteClient remoteClient = field.getAnnotation(RemoteClient.class);
                    Class<?> clazz = field.getType();
                    if (remoteClient != null) {
                        String serviceBaseUrl = remoteClient.value();
                        serviceBaseUrl = StringUtils.cleanPath(serviceBaseUrl);
                        HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
                        httpInvokerProxyFactoryBean.setServiceInterface(clazz);
                        httpInvokerProxyFactoryBean.setServiceUrl(serviceBaseUrl + clazz.getSimpleName());
                        String remoteBeanName = field.getName();
                        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerProxyFactoryBean.class, () -> httpInvokerProxyFactoryBean);
                        beanDefinitionBuilder.setLazyInit(true);
                        beanDefinitionRegistry.registerBeanDefinition(remoteBeanName, beanDefinitionBuilder.getBeanDefinition());
//                        beanDefinition.getPropertyValues().add(field.getName(), new RuntimeBeanReference(remoteBeanName));
                    }
                });

            } catch (ClassNotFoundException e) {
                if (log.isDebugEnabled()) {
                    log.warn("Cannot find class " + beanDefinitionName, e);
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
}
