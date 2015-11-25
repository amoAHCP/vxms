package org.jacpfx.vertx.services.util;

import io.vertx.core.json.JsonObject;
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
            return host.length() > 1 ? "/".concat(host).concat("-").concat(path.value()) : path.value();
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
            return config.getString("host", HOST);
        } else {
            // TODO define Exception !!!
        }
        return "";
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
