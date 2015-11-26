package org.jacpfx.vertx.services.util;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ReflectionUtil {


    public static Object[] invokeWebSocketParameters(byte[] payload, Method method, WebSocketEndpoint endpoint, WebSocketRegistry webSocketRegistry, Vertx vertx, Throwable t) {
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final Object[] parameterResult = new Object[parameters.length];
        int i = 0;

        for (java.lang.reflect.Parameter p : parameters) {
            if (WebSocketHandler.class.equals(p.getType())) {
                parameterResult[i] = new WebSocketHandler(webSocketRegistry, endpoint, payload, vertx);
            } else if (WebSocketEndpoint.class.equals(p.getType())) {
                parameterResult[i] = endpoint;
            } else if (Throwable.class.isAssignableFrom(p.getType())) {
                parameterResult[i] = t;
            }

            i++;
        }

        return parameterResult;
    }

    public static Object[] invokeWebSocketParameters(Method method, WebSocketEndpoint endpoint) {
        return invokeWebSocketParameters(null, method, endpoint, null, null, null);
    }


    public static Object[] invokeWebSocketParameters(Method method, WebSocketEndpoint endpoint, Throwable t) {
        return invokeWebSocketParameters(null, method, endpoint, null, null, t);
    }

    public static void genericMethodInvocation(Method method, Supplier<Object[]> supplier, Object invokeTo) throws Throwable {
        try {
            final Object returnValue = method.invoke(invokeTo, supplier.get());
            if (returnValue != null) {
                // TODO throw exception, no return value expected
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw e;
        }
    }


}
