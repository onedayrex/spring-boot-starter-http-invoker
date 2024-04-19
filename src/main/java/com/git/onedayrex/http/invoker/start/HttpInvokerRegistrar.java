package com.git.onedayrex.http.invoker.start;

import com.git.onedayrex.http.invoker.start.server.RemoteServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author onedayrex
 * HttpInvokerRegistrar
 * when use {@link EnableHttpInvokers}
 * it will register HttpInvoker Server And Client
 */
public class HttpInvokerRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {
    public static final Logger log = LoggerFactory.getLogger(HttpInvokerRegistrar.class);

    private Environment environment;

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registerDefaultServerConfiguration(metadata,registry);
    }

    /**
     * scan for http invoker server and register to spring
     * it will register the server service to use default bean name with interface name
     * @param metadata
     * @param registry
     */
    private void registerDefaultServerConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RemoteServer.class));
        Set<String> basePackages = getBasePackages(metadata);
        for (String basePackage : basePackages) {
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }

        for (BeanDefinition beanDefinition : candidateComponents) {
            if(beanDefinition instanceof AnnotatedBeanDefinition){
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(RemoteServer.class.getCanonicalName());
                Class<?> serviceInterface = (Class<?>) annotationAttributes.get("interfaceClass");
                String serviceInterfaceName = serviceInterface.getSimpleName();
                HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
                httpInvokerServiceExporter.setServiceInterface(serviceInterface);
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerServiceExporter.class, () -> httpInvokerServiceExporter);
                beanDefinitionBuilder.addDependsOn(serviceInterfaceName);
                beanDefinitionBuilder.addPropertyReference("service", serviceInterfaceName);
                beanDefinitionBuilder.setLazyInit(true);
                String path = (String) annotationAttributes.get("path");
                String beanName = path + "/" + serviceInterfaceName;
                BeanDefinitionHolder originBeanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, serviceInterfaceName);
                BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(), beanName, null);
                BeanDefinitionReaderUtils.registerBeanDefinition(originBeanDefinitionHolder, registry);
                BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
            }
        }

    }

    /**
     * resolve {@link EnableHttpInvokers} packages
     * @param importingClassMetadata
     * @return packages
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableHttpInvokers.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }


    /**
     * create a Scanner to scan {@link RemoteServer} and {@link  com.git.onedayrex.http.invoker.start.client.RemoteClient}
     * @return
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
