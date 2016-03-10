package org.jacpfx.vertx.websocket.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jacpfx.vertx.services.util.ConfigurationUtil;
import org.jacpfx.vertx.services.util.ReflectionUtil;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketClose;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketError;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketOpen;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 18.12.15.
 */
public class WebSocketInitializer {


    private static final Logger log = LoggerFactory.getLogger(WebSocketInitializer.class);


    public static void registerWebSocketHandler(HttpServer server, Vertx vertx, WebSocketRegistry webSocketRegistry, JsonObject config, Object service) {
        server.websocketHandler((serverSocket) -> {
            if (serverSocket.path().equals("wsServiceInfo")) {
                // TODO implement serviceInfo request
                return;
            }
            logDebug("connect socket to path: " + serverSocket.path());
            final String path = serverSocket.path();
            final String sName = ConfigurationUtil.serviceName(config, service.getClass());
            if (path.startsWith(sName)) {
                serverSocket.pause();
                final List<Method> webSocketMethodsForURL = getWebSocketMethods(service, path, sName);
                if (webSocketMethodsForURL.isEmpty()) {
                    serverSocket.reject();
                } else {
                    webSocketRegistry.registerAndExecute(serverSocket, endpoint -> {
                        log("register:+ " + endpoint.getUrl());
                        webSocketMethodsForURL.stream().forEach(method ->
                                mapMethodsToWSOperations(vertx, webSocketRegistry, service, serverSocket, webSocketMethodsForURL, endpoint, method));
                        serverSocket.resume();
                    });
                }


            }

        });
    }

    private static void mapMethodsToWSOperations(Vertx vertx, WebSocketRegistry webSocketRegistry, Object service, ServerWebSocket serverSocket, List<Method> webSocketMethodsForURL, WebSocketEndpoint endpoint, Method method) {
        if (method.isAnnotationPresent(OnWebSocketMessage.class)) {
            final Optional<Method> onErrorMethod = webSocketMethodsForURL.stream().filter(m -> m.isAnnotationPresent(OnWebSocketError.class)).findFirst();
            serverSocket.handler(handler -> invokeEndpoint(vertx, webSocketRegistry, service, serverSocket, endpoint, method, onErrorMethod, handler)
            );
        } else if (method.isAnnotationPresent(OnWebSocketOpen.class)) {
            try {
                invokeWebSocketOnOpenCloseMethod(method, endpoint, service);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else if (method.isAnnotationPresent(OnWebSocketClose.class)) {
            // TODO unregister at registry
            serverSocket.closeHandler(close -> {
                try {
                    invokeWebSocketOnOpenCloseMethod(method, endpoint, service);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } else if (method.isAnnotationPresent(OnWebSocketError.class)) {
            // TODO unregister at registry
            serverSocket.exceptionHandler(exception -> {
                try {
                    invokeWebSocketOnErrorMethod(new byte[0], method, endpoint, exception, service, webSocketRegistry, vertx);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private static void invokeEndpoint(Vertx vertx, WebSocketRegistry webSocketRegistry, Object service, ServerWebSocket serverSocket, WebSocketEndpoint endpoint, Method method, Optional<Method> onErrorMethod, Buffer handler) {
        log("invoke endpoint " + endpoint.getUrl());
        try {
            invokeWebSocketMethod(handler.getBytes(), method, onErrorMethod, endpoint, service, webSocketRegistry, serverSocket, vertx);
        } catch (final Throwable throwable) {
            handleException(handler.getBytes(), onErrorMethod, endpoint, service, webSocketRegistry, serverSocket, vertx, throwable);

        }
        log("RUN:::::");
    }

    private static List<Method> getWebSocketMethods(Object service, String path, String sName) {
        final String methodName = path.replace(sName, "");
        final Method[] declaredMethods = service.getClass().getDeclaredMethods();
        return Stream.of(declaredMethods).
                filter(method -> filterWebSocketMethods(method, methodName)).collect(Collectors.toList());
    }

    private static boolean filterWebSocketMethods(final Method method, final String methodName) {
        if (method.isAnnotationPresent(OnWebSocketMessage.class) && method.getAnnotation(OnWebSocketMessage.class).value().equalsIgnoreCase(methodName))
            return true;
        if (method.isAnnotationPresent(OnWebSocketOpen.class) && method.getAnnotation(OnWebSocketOpen.class).value().equalsIgnoreCase(methodName))
            return true;
        if (method.isAnnotationPresent(OnWebSocketClose.class) && method.getAnnotation(OnWebSocketClose.class).value().equalsIgnoreCase(methodName))
            return true;
        return method.isAnnotationPresent(OnWebSocketError.class) && method.getAnnotation(OnWebSocketError.class).value().equalsIgnoreCase(methodName);

    }

    private static void invokeWebSocketMethod(byte[] payload, Method method, final Optional<Method> onErrorMethod, WebSocketEndpoint endpoint, Object service, WebSocketRegistry webSocketRegistry, ServerWebSocket serverSocket, Vertx vertx) throws Throwable {
        ReflectionUtil.genericMethodInvocation(
                method,
                () -> ReflectionUtil.invokeWebSocketParameters(
                        payload,
                        method,
                        endpoint,
                        webSocketRegistry,
                        vertx,
                        null,
                        throwable -> handleException(payload, onErrorMethod, endpoint, service, webSocketRegistry, serverSocket, vertx, throwable)), service);

    }

    private static void handleException(byte[] payload, Optional<Method> onErrorMethod, WebSocketEndpoint endpoint, Object service, WebSocketRegistry webSocketRegistry, ServerWebSocket serverSocket, Vertx vertx, Throwable throwable) {
        if (onErrorMethod.isPresent()) {
            try {
                invokeWebSocketOnErrorMethod(payload, onErrorMethod.get(), endpoint, throwable, service, webSocketRegistry, vertx);
            } catch (Throwable throwable1) {
                serverSocket.close();
                throwable1.printStackTrace();
            }
        } else {
            serverSocket.close();
            throwable.printStackTrace();
        }
    }

    private static void invokeWebSocketOnOpenCloseMethod(Method method, WebSocketEndpoint endpoint, Object service) throws Throwable {
        ReflectionUtil.genericMethodInvocation(method, () -> ReflectionUtil.invokeWebSocketParameters(method, endpoint), service);
    }

    private static void invokeWebSocketOnErrorMethod(byte[] payload, Method method, WebSocketEndpoint endpoint, Throwable t, Object service, WebSocketRegistry webSocketRegistry, Vertx vertx) throws Throwable {
        ReflectionUtil.genericMethodInvocation(method, () -> ReflectionUtil.invokeWebSocketParameters(payload, method, endpoint, webSocketRegistry, vertx, t, null), service);
    }

    private static void logDebug(String message) {
        log.debug(message);
    }

    private static void log(final String value) {
        log.info(value);
    }
}
