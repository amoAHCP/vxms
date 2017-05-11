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

package org.jacpfx.vertx.util;

import java.util.ServiceLoader;
import or.jacpfx.spi.EventhandlerSPI;
import or.jacpfx.spi.RESThandlerSPI;
import or.jacpfx.spi.ServiceDiscoverySpi;
import or.jacpfx.spi.WebSockethandlerSPI;
import org.jacpfx.common.configuration.DefaultEndpointConfiguration;
import org.jacpfx.common.configuration.EndpointConfig;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * General Utility class for resolving SPI and Endpoint configuration
 * Created by amo on 24.10.16.
 */
public class ServiceUtil {

  /**
   * extract the endpoint configuration fro service
   *
   * @param service the service where to extract the endpoint configuration
   * @return the {@link EndpointConfiguration}
   */
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

  /**
   * Returns all service discovery implementations
   *
   * @return an implementation of {@link ServiceDiscoverySpi}
   */
  public static ServiceDiscoverySpi getServiceDiscoverySPI() {
    ServiceLoader<ServiceDiscoverySpi> loader = ServiceLoader.load(ServiceDiscoverySpi.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator().next();
  }

  /**
   * Returns all REST implementations
   *
   * @return an implementation of {@link RESThandlerSPI}
   */
  public static RESThandlerSPI getRESTSPI() {
    ServiceLoader<RESThandlerSPI> loader = ServiceLoader.load(RESThandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator().next();
  }

  /**
   * Returns all event-bus implementations
   *
   * @return an implementation of {@link EventhandlerSPI}
   */
  public static EventhandlerSPI getEventBusSPI() {
    ServiceLoader<EventhandlerSPI> loader = ServiceLoader.load(EventhandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator().next();
  }

  /**
   * Returns all websocket implementations
   *
   * @return an implementation of {@link WebSockethandlerSPI}
   */
  public static WebSockethandlerSPI getWebSocketSPI() {
    ServiceLoader<WebSockethandlerSPI> loader = ServiceLoader.load(WebSockethandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator().next();
  }
}