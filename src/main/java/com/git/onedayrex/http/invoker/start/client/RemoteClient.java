package com.git.onedayrex.http.invoker.start.client;

import javax.annotation.Resource;
import java.lang.annotation.*;

@Resource
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface RemoteClient {

    String value() default "";

}
