package com.git.onedayrex.http.invoker.start.client;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RemoteClient {

    String serviceUrl() default "";

}
