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
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.common.util.URIUtil;
import org.jacpfx.vertx.event.annotation.Consume;
import org.jacpfx.vertx.event.annotation.OnEventError;
import org.jacpfx.vertx.event.response.EventbusHandler;
import org.jacpfx.vertx.event.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @param vertx   the Vert.x instance
     * @param service the Vxms service object itself
     */
    public static void initEventbusHandling(Vertx vertx, Object service) {
        Stream.of(service.getClass().getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(Consume.class)).
                forEach(restMethod -> initEventbusMethod(vertx, service, restMethod));
    }

    /**
     * Initialize a specific REST method from Service
     *
     * @param vertx          The Vertx instance
     * @param service        The Service itself
     * @param eventBusMethod the event-bus Method
     */
    public static void initEventbusMethod(Vertx vertx, Object service, Method eventBusMethod) {
        final Consume path = eventBusMethod.getAnnotation(Consume.class);
        final Optional<Method> errorMethod = getEventbusMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnEventError.class)).findFirst();
        Optional.ofNullable(path).ifPresent(g -> initCallback(vertx, service, eventBusMethod, path, errorMethod));


    }

    protected static void initCallback(Vertx vertx, Object service, Method eventBusMethod, Consume path, Optional<Method> errorMethod) {
        final String contexRoot = URIUtil.getCleanContextRoot(ConfigurationUtil.getContextRoot(vertx.getOrCreateContext().config(), service.getClass()));
        final String route = contexRoot + URIUtil.cleanPath(path.value());
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + EVENTBUS + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        registerCallback(methodId, route, vertx, service, eventBusMethod, errorMethod);
    }



    private static void registerCallback(String methodId, String route, Vertx vertx, Object service, Method eventBusMethod, Optional<Method> errorMethod) {
        vertx.eventBus().consumer(route, eventbusHandler -> handleIncomingEvent(methodId, vertx, service, eventBusMethod, errorMethod, eventbusHandler));

    }

    private static void handleIncomingEvent(String methodId, Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, Message<Object> eventbusHandler) {
        try {
            final Object[] parameters = getInvocationParameters(methodId, vertx, service, restMethod, onErrorMethod, eventbusHandler);
            ReflectionUtil.genericMethodInvocation(restMethod, () -> parameters, service);
        } catch (Throwable throwable) {
            handleEventBusError(methodId + "ERROR", vertx, service, onErrorMethod, eventbusHandler, throwable);
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
        return method.isAnnotationPresent(Consume.class) && method.getAnnotation(Consume.class).value().equalsIgnoreCase(methodName) ||
                method.isAnnotationPresent(OnEventError.class) && method.getAnnotation(OnEventError.class).value().equalsIgnoreCase(methodName);

    }


    private static Object[] getInvocationParameters(String methodId, Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, Message<Object> eventbusHandler) {
        final Consumer<Throwable> throwableConsumer = throwable -> handleEventBusError(methodId + "ERROR", vertx, service, onErrorMethod, eventbusHandler, throwable);
        return ReflectionUtil.invokeParameters(
                restMethod,
                null,
                new EventbusHandler(methodId, eventbusHandler, vertx, null, throwableConsumer));
    }

    private static void handleEventBusError(String methodId, Vertx vertx, Object service, Optional<Method> onErrorMethod, Message<Object> eventbusHandler, Throwable throwable) {
        if (onErrorMethod.isPresent()) {
            invokeOnErrorMethod(methodId, vertx, service, onErrorMethod, eventbusHandler, throwable);
        } else {
            failRequest(eventbusHandler, throwable);
        }
    }


    private static void invokeOnErrorMethod(String methodId, Vertx vertx, Object service, Optional<Method> onErrorMethod, Message<Object> eventbusHandler, Throwable throwable) {
        onErrorMethod.ifPresent(errorMethod -> {
            try {
                ReflectionUtil.genericMethodInvocation(errorMethod, () ->
                        ReflectionUtil.invokeParameters(errorMethod, throwable, new EventbusHandler(methodId, eventbusHandler, vertx, throwable, null)), service);
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
