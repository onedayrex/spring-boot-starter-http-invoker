package com.git.onedayrex.http.invoker.start;

import com.git.onedayrex.http.invoker.start.client.RemoteClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class HttpInvokerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> parentClazz = bean.getClass();
        Field[] declaredFields = parentClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(RemoteClient.class)) {
                Object remoteBean = applicationContext.getBean(field.getName());
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, remoteBean);
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
