package com.git.onedayrex.http.invoker.start.server;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RemoteServer {

    String path() default "/api";

    Class<?> interfaceClass() default Void.class;
}
