/*
 * Copyright [2018] [Andy Moncsek]
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

package org.jacpfx.vxms.common.util;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.jacpfx.vxms.spi.EventhandlerSPI;
import org.jacpfx.vxms.spi.RESThandlerSPI;
import org.jacpfx.vxms.spi.ServiceDiscoverySPI;
import org.jacpfx.vxms.spi.WebSockethandlerSPI;

/**
 * General Utility class for resolving SPI extensions
 * Created by amo on 24.10.16.
 */
public class ServiceUtil {


  /**
   * Returns all discovery implementations
   *
   * @return an implementation of {@link org.jacpfx.vxms.spi.ServiceDiscoverySPI}
   */
  public static ServiceDiscoverySPI getDiscoverySPI() {
    ServiceLoader<ServiceDiscoverySPI> loader = ServiceLoader.load(ServiceDiscoverySPI.class);
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
  public static Iterator<RESThandlerSPI> getRESTSPI() {
    ServiceLoader<RESThandlerSPI> loader = ServiceLoader.load(RESThandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator();
  }

  /**
   * Returns all event-bus implementations
   *
   * @return an implementation of {@link EventhandlerSPI}
   */
  public static Iterator<EventhandlerSPI> getEventBusSPI() {
    ServiceLoader<EventhandlerSPI> loader = ServiceLoader.load(EventhandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator();
  }

  /**
   * Returns all websocket implementations
   *
   * @return an implementation of {@link WebSockethandlerSPI}
   */
  public static Iterator<WebSockethandlerSPI> getWebSocketSPI() {
    ServiceLoader<WebSockethandlerSPI> loader = ServiceLoader.load(WebSockethandlerSPI.class);
    if (!loader.iterator().hasNext()) {
      return null;
    }
    return loader.iterator();
  }
}
