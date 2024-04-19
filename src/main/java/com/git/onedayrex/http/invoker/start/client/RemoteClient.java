package com.git.onedayrex.http.invoker.start.client;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface RemoteClient {

    String value() default "";

}
