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

    public static String getEndpointHost(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return config.getString("host", selfHosted.host());
        }
        return config.getString("host", getHostName());
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
