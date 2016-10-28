package org.jacpfx.vertx.util;

import or.jacpfx.spi.RESThandlerSPI;
import or.jacpfx.spi.ServiceDiscoverySpi;
import or.jacpfx.spi.WebSockethandlerSPI;
import org.jacpfx.common.configuration.DefaultEndpointConfiguration;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.common.configuration.EndpointConfiguration;

import java.util.ServiceLoader;

/**
 * Created by amo on 24.10.16.
 */
public class ServiceUtil {
    public static final String SLASH = "/";

    public static String getCleanContextRoot(String contextRoot) {
        if (String.valueOf(contextRoot.charAt(contextRoot.length() - 1)).equals(SLASH)) {
            String _root = contextRoot.substring(0, contextRoot.length() - 1);
            return _root.startsWith(SLASH) ? _root : SLASH + _root;
        } else if (!contextRoot.startsWith(SLASH)) {
            return SLASH + contextRoot;
        }
        return contextRoot;
    }

    public static EndpointConfiguration getEndpointConfiguration(Object service) {
        EndpointConfiguration endpointConfig = null;
        if (service.getClass().isAnnotationPresent(EndpointConfig.class)) {
            final EndpointConfig annotation = service.getClass().getAnnotation(EndpointConfig.class);
            final Class<? extends EndpointConfiguration> epConfigClazz = annotation.value();
            try {
                endpointConfig = epConfigClazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return endpointConfig == null ? new DefaultEndpointConfiguration() : endpointConfig;
    }

    public static ServiceDiscoverySpi getServiceDiscoverySPI() {
        ServiceLoader<ServiceDiscoverySpi> loader = ServiceLoader.load(ServiceDiscoverySpi.class);
        if (!loader.iterator().hasNext()) return null;
        return loader.iterator().next();
    }

    public static RESThandlerSPI getRESTSPI() {
        ServiceLoader<RESThandlerSPI> loader = ServiceLoader.load(RESThandlerSPI.class);
        if (!loader.iterator().hasNext()) return null;
        return loader.iterator().next();
    }

    public static WebSockethandlerSPI getWebSocketSPI() {
        ServiceLoader<WebSockethandlerSPI> loader = ServiceLoader.load(WebSockethandlerSPI.class);
        if (!loader.iterator().hasNext()) return null;
        return loader.iterator().next();
    }
}
