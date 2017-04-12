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

package org.jacpfx.vertx.event;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.common.util.URIUtil;
import org.jacpfx.vertx.event.annotation.Consume;
import org.jacpfx.vertx.event.annotation.OnEventError;
import org.jacpfx.vertx.event.response.EventbusHandler;
import org.jacpfx.vertx.event.util.ReflectionUtil;

/**
 * Created by Andy Moncsek on 09.03.16.
 */
public class EventInitializer {


  public static final String ROOT = "/";
  public static final String HTTP_ALL = "ALL";
  public static final String EVENTBUS = "eventbus";

  /**
   * initialize default Event Bus implementation for vxms
   *
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param service the Vxms service object itself
   */
  public static void initEventbusHandling(VxmsShared vxmsShared, Object service) {
    Stream.of(service.getClass().getDeclaredMethods()).
        filter(m -> m.isAnnotationPresent(Consume.class)).
        forEach(restMethod -> initEventbusMethod(vxmsShared, service, restMethod));
  }

  /**
   * Initialize a specific REST method from Service
   *
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param service The Service itself
   * @param eventBusMethod the event-bus Method
   */
  public static void initEventbusMethod(VxmsShared vxmsShared, Object service,
      Method eventBusMethod) {
    final Consume path = eventBusMethod.getAnnotation(Consume.class);
    final Optional<Method> errorMethod = getEventbusMethods(service, path.value()).stream()
        .filter(method -> method.isAnnotationPresent(OnEventError.class)).findFirst();
    Optional.ofNullable(path)
        .ifPresent(g -> initCallback(vxmsShared, service, eventBusMethod, path, errorMethod));


  }

  protected static void initCallback(VxmsShared vxmsShared, Object service, Method eventBusMethod,
      Consume path, Optional<Method> errorMethod) {
    final Vertx vertx = vxmsShared.getVertx();
    final String contexRoot = URIUtil.getCleanContextRoot(
        ConfigurationUtil.getContextRoot(vertx.getOrCreateContext().config(), service.getClass()));
    final String route = contexRoot + URIUtil.cleanPath(path.value());
    final Context context = vertx.getOrCreateContext();
    final String methodId =
        path.value() + EVENTBUS + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
    registerCallback(methodId, route, vxmsShared, service, eventBusMethod, errorMethod);
  }


  private static void registerCallback(String methodId, String route, VxmsShared vxmsShared,
      Object service, Method eventBusMethod, Optional<Method> errorMethod) {
    final Vertx vertx = vxmsShared.getVertx();
    vertx.eventBus().consumer(route,
        eventbusHandler -> handleIncomingEvent(methodId, vxmsShared, service, eventBusMethod,
            errorMethod, eventbusHandler));

  }

  private static void handleIncomingEvent(String methodId, VxmsShared vxmsShared, Object service,
      Method restMethod, Optional<Method> onErrorMethod, Message<Object> eventbusHandler) {
    try {
      final Object[] parameters = getInvocationParameters(methodId, vxmsShared, service, restMethod,
          onErrorMethod, eventbusHandler);
      ReflectionUtil.genericMethodInvocation(restMethod, () -> parameters, service);
    } catch (Throwable throwable) {
      handleEventBusError(methodId + "ERROR", vxmsShared, service, onErrorMethod, eventbusHandler,
          throwable);
    }
  }


  private static List<Method> getEventbusMethods(Object service, String sName) {
    final String methodName = sName;
    final Method[] declaredMethods = service.getClass().getDeclaredMethods();
    return Stream.
        of(declaredMethods).
        filter(method -> filterEventbusMethods(method, methodName)).
        collect(Collectors.toList());
  }

  private static boolean filterEventbusMethods(final Method method, final String methodName) {
    return method.isAnnotationPresent(Consume.class) && method.getAnnotation(Consume.class).value()
        .equalsIgnoreCase(methodName) ||
        method.isAnnotationPresent(OnEventError.class) && method.getAnnotation(OnEventError.class)
            .value().equalsIgnoreCase(methodName);

  }


  private static Object[] getInvocationParameters(String methodId, VxmsShared vxmsShared,
      Object service, Method restMethod, Optional<Method> onErrorMethod,
      Message<Object> eventbusHandler) {
    final Consumer<Throwable> throwableConsumer = throwable -> handleEventBusError(
        methodId + "ERROR", vxmsShared, service, onErrorMethod, eventbusHandler, throwable);
    return ReflectionUtil.invokeParameters(
        restMethod,
        null,
        new EventbusHandler(methodId, eventbusHandler, vxmsShared, null, throwableConsumer));
  }

  private static void handleEventBusError(String methodId, VxmsShared vxmsShared, Object service,
      Optional<Method> onErrorMethod, Message<Object> eventbusHandler, Throwable throwable) {
    if (onErrorMethod.isPresent()) {
      invokeOnErrorMethod(methodId, vxmsShared, service, onErrorMethod, eventbusHandler, throwable);
    } else {
      failRequest(eventbusHandler, throwable);
    }
  }


  private static void invokeOnErrorMethod(String methodId, VxmsShared vxmsShared, Object service,
      Optional<Method> onErrorMethod, Message<Object> eventbusHandler, Throwable throwable) {
    onErrorMethod.ifPresent(errorMethod -> {
      try {
        ReflectionUtil.genericMethodInvocation(errorMethod, () ->
                ReflectionUtil.invokeParameters(errorMethod, throwable,
                    new EventbusHandler(methodId, eventbusHandler, vxmsShared, throwable, null)),
            service);
      } catch (Throwable t) {
        failRequest(eventbusHandler, t);
      }

    });
  }

  private static void failRequest(Message<Object> eventbusHandler, Throwable throwable) {
    eventbusHandler.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), throwable.getMessage());
    throwable.printStackTrace();
  }


}
