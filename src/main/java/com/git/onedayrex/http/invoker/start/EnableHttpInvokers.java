package com.git.onedayrex.http.invoker.start;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author onedayrex
 * Scans for interfaces that declare they are http invoker
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(HttpInvokerRegistrar.class)
public @interface EnableHttpInvokers {

    /**
     * Base package to scan
     */
    String[] value() default {};


    /**
     * Base packages to scan
     */
    String[] basePackages() default {};


    /**
     * Scan class,it will scan class package
     */
    Class<?>[] basePackageClasses() default {};

}
