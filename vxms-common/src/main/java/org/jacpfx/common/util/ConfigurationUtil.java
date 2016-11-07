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


    public static String getStringConfiguration(final JsonObject config, String propertyName, String defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return env;
        return config.getString(propertyName, defaultValue);
    }

    public static Integer getIntegerConfiguration(final JsonObject config, String propertyName, int defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return Integer.valueOf(env);
        return config.getInteger(propertyName, defaultValue);
    }


    public static String getServiceName(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint name = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "service-name", name.name());
        }
        return getStringConfiguration(config, "service-name", clazz.getSimpleName());
    }


    public static Integer getEndpointPort(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getIntegerConfiguration(config, "port", endpoint.port());
        }
        return getIntegerConfiguration(config, "port", 8080);
    }

    public static String getEndpointHost(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "host", selfHosted.host());
        }
        return getStringConfiguration(config, "host", getHostName());
    }

    public static String getContextRoot(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "context-root", endpoint.contextRoot());
        }
        return getStringConfiguration(config, "context-root", "/");
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

    public static String getHostName() {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }
}
