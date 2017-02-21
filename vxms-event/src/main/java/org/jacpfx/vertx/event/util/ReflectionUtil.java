package org.jacpfx.vertx.event.util;

import org.jacpfx.common.exceptions.EndpointExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ReflectionUtil {


    public static <T> Object[] invokeParameters(Method method, Throwable t, T handler) {
        method.setAccessible(true);
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final Object[] parameterResult = new Object[parameters.length];
        int i = 0;
        for (java.lang.reflect.Parameter p : parameters) {
            if (handler!=null && handler.getClass().equals(p.getType())) {
                parameterResult[i] = handler;
            }
            if (Throwable.class.isAssignableFrom(p.getType())) {
                parameterResult[i] = t;
            }
            i++;
        }
        return parameterResult;
    }


    public static void genericMethodInvocation(Method method, Supplier<Object[]> parameters, Object invokeTo) throws Throwable {
        try {
            method.invoke(invokeTo, parameters.get());
        } catch (IllegalAccessException e) {
            e.printStackTrace();

        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof EndpointExecutionException) throw e.getCause().getCause();
            throw e.getTargetException();
        } catch (Exception e) {
            throw e;
        }
    }


}
