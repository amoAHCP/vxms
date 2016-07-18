package org.jacpfx.common.util;

import io.vertx.core.json.JsonObject;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.DefaultServerOptions;
import org.jacpfx.common.ServiceEndpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ConfigurationUtil {

    private static final String HOST = getHostName();
    private static final String HOST_PREFIX = "";

    public static String serviceName(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {

            final String host = config.getString("host-prefix", HOST_PREFIX);
            final org.jacpfx.common.ServiceEndpoint path = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return host.length() > 1 ? "/".concat(host).concat("-").concat(path.name()) : path.name();
        } else {
            // TODO define Exception !!!
        }
        return null;
    }


    public static Integer getEndpointPort(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getInteger("port", selfHosted.port());
        } else {
            // TODO define Exception !!!
        }
        return 0;
    }

    public static String getEndpointHost(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getString("host", selfHosted.host());
        }
        return config.getString("host", HOST);
    }

    public static CustomServerOptions getEndpointOptions(Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            try {
                return selfHosted.options().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new DefaultServerOptions();
    }

    // TODO should not be used
    @Deprecated
    public static String getHostName() {
        try {
            InetAddress.getLocalHost().getHostName();
            return "0.0.0.0";
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
