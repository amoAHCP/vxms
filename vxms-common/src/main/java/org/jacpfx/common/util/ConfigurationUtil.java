/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.common.util;

import io.vertx.core.json.JsonObject;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.DefaultServerOptions;
import org.jacpfx.common.ServiceEndpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by Andy Moncsek on 25.11.15.
 * Provides several methods to get the correct configuration
 */
public class ConfigurationUtil {


    /**
     * Returns a String configuration value by key or return default value
     * @param config, the configuration object
     * @param propertyName the property name to look for
     * @param defaultValue the fallback value
     * @return the String configuration value
     */
    public static String getStringConfiguration(final JsonObject config, String propertyName, String defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return env;
        return config.getString(propertyName, defaultValue);
    }
    /**
     * Returns an Integer configuration value by key or return default value
     * @param config, the configuration object
     * @param propertyName the property name to look for
     * @param defaultValue the fallback value
     * @return the Integer configuration value
     */
    public static Integer getIntegerConfiguration(final JsonObject config, String propertyName, int defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return Integer.valueOf(env);
        return config.getInteger(propertyName, defaultValue);
    }


    /**
     * Returns the service name defined in {@link ServiceEndpoint} annotation or passed by configuration
     * @param config, the configuration object
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return the Service name
     */
    public static String getServiceName(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint name = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "service-name", name.name());
        }
        return getStringConfiguration(config, "service-name", clazz.getSimpleName());
    }


    /**
     * Returns the defined PORT to listen to
     * @param config, the configuration object
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return the defined PORT number
     */
    public static Integer getEndpointPort(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getIntegerConfiguration(config, "port", endpoint.port());
        }
        return getIntegerConfiguration(config, "port", 8080);
    }

    /**
     * Returns the defined HOST name to use for HTTP listening
     * @param config, the configuration object
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return the defined HOST name
     */
    public static String getEndpointHost(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "host", selfHosted.host());
        }
        return getStringConfiguration(config, "host", getHostName());
    }

    /**
     * Returns the http Root context defined in {@link ServiceEndpoint} or in configuration
     * @param config, the configuration object
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return tzhe defined Root context of your service
     */
    public static String getContextRoot(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "context-root", endpoint.contextRoot());
        }
        return getStringConfiguration(config, "context-root", "/");
    }

    /**
     * Returns the Method id's Postfix. This Id is used for the stateful circuit breaker as key in a shared map. The consequence is: If value is "unique" you get a random UUID, so for each method an unique shared state will be maintained.
     * If value is "local" the process PID will be returned, so all instances in one JVM will share on lock. In case of global, the postfix ist an empty String, so all instances (the same method signature) in the cluster will share this lock.
     * @param config, the configuration object
     * @return the correct POSTFIX for method id's
     */
    public static String getCircuitBreakerIDPostfix(final JsonObject config) {
        final String configValue = getStringConfiguration(config, "circuit-breaker-scope", "unique");
        switch (configValue){
            case "unique":
                return UUID.randomUUID().toString();
            case "global":
                return "";
            case "local":
                return getPID();
            default:
                return UUID.randomUUID().toString();
        }
    }

    /**
     * Returns the current PID of your JVM instance.
     * @return the PID number as a String
     */
    private static String getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName != null && processName.length() > 0) {
            try {
                return processName.split("@")[0];
            }
            catch (Exception e) {
                return "0";
            }
        }

        return "0";
    }

    /**
     * Returns the endpoint configuration object, defined in ServerEndoint annotation. If no definition is present a DefaultServerOptions instance will be created.
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return {@link CustomServerOptions} the Endpoint configuration
     */
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

    /**
     * Returns the current HOST name
     * @return the HOST name
     */
    private static String getHostName() {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }
}
