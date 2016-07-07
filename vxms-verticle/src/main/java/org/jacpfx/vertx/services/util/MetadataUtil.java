package org.jacpfx.vertx.services.util;

import io.vertx.core.json.JsonObject;
import org.jacpfx.common.Operation;
import org.jacpfx.common.ServiceInfo;
import org.jacpfx.common.Type;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 15.06.16.
 * This Util class creates a Service info descriptor of the class to analyze with all its REST and WebSocket methods
 */
public class MetadataUtil {

    public static ServiceInfo createInfoObject(Integer port, JsonObject config, Class<?> classToAnalyse) {
        final List<Operation> operations = getDeclaredOperations(config, classToAnalyse);
        return new ServiceInfo(ConfigurationUtil.serviceName(config, classToAnalyse), null, ConfigurationUtil.getHostName(), null, null, port, operations.toArray(new Operation[operations.size()]));
    }

    private static List<Operation> getDeclaredOperations(JsonObject config, Class<?> classToAnalyse) {
        final List<Operation> allWebSocketOperationsInService = getAllWebSocketOperationsInService(config, classToAnalyse);
        final List<Operation> allRESTOperationsInService = getAllRESTOperationsInService(config, classToAnalyse);
        return Stream.
                concat(allWebSocketOperationsInService.stream(), allRESTOperationsInService.stream()).
                collect(Collectors.toList());
    }


    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     *
     * @param config         the verticle configuration
     * @param classToAnalyse the class to analyse
     * @return a list of all operation in service
     */
    private static List<Operation> getAllWebSocketOperationsInService(JsonObject config, Class<?> classToAnalyse) {
        final Method[] allMethods = classToAnalyse.getDeclaredMethods();
        return Stream.of(allMethods).
                filter(m -> m.isAnnotationPresent(OnWebSocketMessage.class)).
                map(method -> mapWebSocketMethod(method, config, classToAnalyse)).collect(Collectors.toList());
    }

    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     *
     * @param config         the verticle configuration
     * @param classToAnalyse the class to analyse
     * @return a list of all operation in service
     */
    private static List<Operation> getAllRESTOperationsInService(JsonObject config, Class<?> classToAnalyse) {
        final Method[] allMethods = classToAnalyse.getDeclaredMethods();
        return Stream.of(allMethods).
                filter(m -> m.isAnnotationPresent(Path.class)).
                map(method -> mapRESTtMethod(method, config, classToAnalyse)).collect(Collectors.toList());
    }

    private static Operation mapWebSocketMethod(Method method, JsonObject config, Class<?> classToAnalyse) {
        final OnWebSocketMessage path = method.getDeclaredAnnotation(OnWebSocketMessage.class);
        // TODO remove Produces and Consumes?
        final Produces produces = method.getDeclaredAnnotation(Produces.class);
        final Consumes consumes = method.getDeclaredAnnotation(Consumes.class);
        if (path == null)
            throw new MissingResourceException("missing OperationType ", classToAnalyse.getName(), "");
        final String[] mimeTypes = produces != null ? produces.value() : null;
        final String[] consumeTypes = consumes != null ? consumes.value() : null;
        final String url = Optional.ofNullable(ConfigurationUtil.serviceName(config, classToAnalyse)).orElse("").concat(path.value());
        final List<String> parameters = new ArrayList<>();

        parameters.addAll(getWSParameter(method));

        // TODO add service description (description about service itself)!!!
        return new Operation(path.value(), null, url, Type.WEBSOCKET.name(), mimeTypes, consumeTypes, parameters.toArray(new String[parameters.size()]));
    }

    private static Operation mapRESTtMethod(Method method, JsonObject config, Class<?> classToAnalyse) {
        final Path path = method.getDeclaredAnnotation(Path.class);
        // TODO implement POST,... create array of Operations for each annotation
        final Produces produces = method.getDeclaredAnnotation(Produces.class);
        final Consumes consumes = method.getDeclaredAnnotation(Consumes.class);
        final GET get = method.getDeclaredAnnotation(GET.class);
        final POST post = method.getDeclaredAnnotation(POST.class);
        final PUT put = method.getDeclaredAnnotation(PUT.class);
        final DELETE delete = method.getDeclaredAnnotation(DELETE.class);
        if (path == null)
            throw new MissingResourceException("missing OperationType ", classToAnalyse.getName(), "");
        final String[] mimeTypes = produces != null ? produces.value() : null;
        final String[] consumeTypes = consumes != null ? consumes.value() : null;
        final String url = Optional.ofNullable(ConfigurationUtil.serviceName(config, classToAnalyse)).orElse("").concat(path.value());
        final List<String> parameters = new ArrayList<>();

        parameters.addAll(getRESTParameter(method));

        // TODO add service description (description about service itself)!!!
        return new Operation(path.value(), null, url, Type.REST_GET.name(), mimeTypes, consumeTypes, parameters.toArray(new String[parameters.size()]));
    }


    /**
     * Retrieving a list (note only one parameter is allowed) of all possible ws method paramaters
     *
     * @param method
     * @return a List of all available parameters on method
     */
    private static List<String> getWSParameter(Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final List<String> classes = Stream.of(parameterTypes).
                filter(component -> !component.equals(WebSocketHandler.class)).
                map(Class::getName).
                collect(Collectors.toList());
        if (classes.size() > 1)
            throw new IllegalArgumentException("only one parameter is allowed -- the message body -- and/or the WSHandler");
        return classes;
    }

    /**
     * Retrieving a list (note only one parameter is allowed) of all possible ws method paramaters
     *
     * @param method
     * @return a List of all available parameters on method
     */
    private static List<String> getRESTParameter(Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final List<String> classes = Stream.of(parameterTypes).
                filter(component -> !component.equals(RestHandler.class)).
                map(Class::getName).
                collect(Collectors.toList());
        if (classes.size() > 1)
            throw new IllegalArgumentException("only one parameter is allowed -- the message body -- and/or the WSHandler");
        return classes;
    }

}
