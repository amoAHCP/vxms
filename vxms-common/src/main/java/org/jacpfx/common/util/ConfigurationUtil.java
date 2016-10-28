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

    public static String getServiceName(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint name = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getString("service-name", name.name());
        }
        return config.getString("service-name", clazz.getSimpleName());
    }


    public static Integer getEndpointPort(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getInteger("port", endpoint.port());
        }
        return config.getInteger("port", 8080);
    }

    // TODO find out current address where the endpoint is listening, especially when running in Docker
    public static String getEndpointHost(final JsonObject config, Class clazz) {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            String host = selfHosted.host().isEmpty() ? hostName : selfHosted.host();
            return config.getString("host", host);
        }
        return config.getString("host", hostName.isEmpty() ? HOST : hostName);
    }

    public static String getContextRoot(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getString("context-root", endpoint.contextRoot());
        }
        return config.getString("context-root", "/");
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
        return "127.0.0.1";
    }
}
