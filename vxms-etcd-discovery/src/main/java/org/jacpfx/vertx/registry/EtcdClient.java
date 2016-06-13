package org.jacpfx.vertx.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Etcd service discovery annotation. Annotate a vxms verticle to activate verticle registration and to allow DiscoveryClient injection.
 * Created by Andy Moncsek on 13.06.16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EtcdClient {

    int port() default 4001;

    String host() default "127.0.0.1";

    String domain() default "default";

    /**
     * ttl in seconds
     * @return  ttl sec. value
     */
    int ttl() default 30;
}
