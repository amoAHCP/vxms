package org.jacpfx.vertx.services.util;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.WSHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ReflectionUtil {


    public static  Object[] invokeWSParameters(byte[] payload, Method method, WebSocketEndpoint endpoint, WebSocketRegistry webSocketRegistry, Vertx vertx) {
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final Object[] parameterResult = new Object[parameters.length];
        int i = 0;

        for (java.lang.reflect.Parameter p : parameters) {
            if (p.getType().equals(WSHandler.class)) {
                parameterResult[i] = new WSHandler(webSocketRegistry, endpoint, payload, vertx);
            }

            i++;
        }

        return parameterResult;
    }

    public static void genericMethodInvocation(Method method, Supplier<Object[]> supplier, Object invokeTo) {
        try {
            final Object returnValue = method.invoke(invokeTo, supplier.get());
            if (returnValue != null) {
                // TODO throw exception, no return value expected
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();

        } catch (InvocationTargetException e) {

        }
    }


}
