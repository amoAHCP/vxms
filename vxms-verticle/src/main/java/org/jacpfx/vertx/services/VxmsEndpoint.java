package org.jacpfx.vertx.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import org.jacpfx.common.Operation;
import org.jacpfx.common.ServiceInfo;
import org.jacpfx.common.Type;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.util.ConfigurationUtil;
import org.jacpfx.vertx.services.util.ReflectionUtil;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.handler.WebSocketInitializer;
import org.jacpfx.vertx.websocket.registry.LocalWebSocketRegistry;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extend a service verticle to provide pluggable sevices for vet.x microservice project
 * Created by amo on 28.10.14.
 */
public abstract class VxmsEndpoint extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VxmsEndpoint.class);
    public static final String REGEX_CHECK = "*";
    private String host;
    private ServiceInfo descriptor;
    private boolean clustered;
    private int port = 0;
    private WebSocketRegistry webSocketRegistry;

    @Override
    public final void start(final Future<Void> startFuture) {
        long startTime = System.currentTimeMillis();
        port = ConfigurationUtil.getEndpointPort(getConfig(), this.getClass());
        host = ConfigurationUtil.getEndpointHost(getConfig(), this.getClass());
        // collect all service operations in service for descriptor

        descriptor = createInfoObject(port);
        // register info (keepAlive) handler
        vertx.eventBus().consumer(ConfigurationUtil.serviceName(getConfig(), this.getClass()) + "-info", this::info);

        initSelfHostedService();

        long endTime = System.currentTimeMillis();
        log.info("start time: " + (endTime - startTime) + "ms");
        startFuture.complete();

    }

    private void initSelfHostedService() {
        if (port > 0) {
            updateConfigurationToSelfhosted();

            clustered = getConfig().getBoolean("clustered", false);

            HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost(host)
                    .setPort(port));
            // TODO check if Websocket is wanted
            final Object service = this;
            descriptor.getOperationsByType(Type.WEBSOCKET).findFirst().ifPresent(operation -> {
                webSocketRegistry = initWebSocketRegistryInstance();
                WebSocketInitializer.registerWebSocketHandler(server, vertx, webSocketRegistry, getConfig(), service);
            });
            Router router = Router.router(vertx);
            descriptor.getOperationsByType(Type.REST_GET).findFirst().ifPresent(operation -> {
                Stream.of(this.getClass().getDeclaredMethods()).
                        filter(m -> m.isAnnotationPresent(Path.class)).
                        forEach(restMethod -> {
                            Path path = restMethod.getAnnotation(Path.class);
                            final String sName = ConfigurationUtil.serviceName(getConfig(), service.getClass());
                            // final boolean isRegEx = path.value().contains(REGEX_CHECK);
                            // TODO add annotation wit config class to allow individual configuration of BodyHandler, CookieHandler, CORSHandler and session
                            router.route().handler(BodyHandler.create());
                            router.route().handler(CookieHandler.create());
                            // TODO add session handling
                            /**
                             * // Create a clustered session store using defaults
                             SessionStore store = ClusteredSessionStore.create(vertx);

                             SessionHandler sessionHandler = SessionHandler.create(store);
                             */
                            router.get(sName+path.value()).handler(routingContext -> {
                                try {
                                    final Object[] parameters = ReflectionUtil.invokeRESTParameters(
                                            routingContext,
                                            restMethod,
                                            vertx,
                                            null,
                                            throwable -> throwable.getStackTrace());
                                    ReflectionUtil.genericMethodInvocation(
                                            restMethod,
                                            () -> parameters, service);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            });
                        });
            });


            server.requestHandler(router::accept).listen();
        }
    }


    private WebSocketRegistry initWebSocketRegistryInstance() {
        if (clustered) {
            return null;
        } else {
            return new LocalWebSocketRegistry(this.vertx);
        }
    }

    private void updateConfigurationToSelfhosted() {
        getConfig().put("selfhosted", true);
        getConfig().put("selfhosted-host", ConfigurationUtil.serviceName(getConfig(), this.getClass()));
    }


    private void logDebug(String message) {
        log.debug(message);
    }

    private void log(final String value) {
        log.info(value);
    }


    private ServiceInfo createInfoObject(Integer port) {
        final List<Operation> operations = getDeclaredOperations();
        return new ServiceInfo(ConfigurationUtil.serviceName(getConfig(), this.getClass()), null, ConfigurationUtil.getHostName(), null, null, port, operations.toArray(new Operation[operations.size()]));
    }

    private List<Operation> getDeclaredOperations() {
        final List<Operation> allWebSocketOperationsInService = getAllWebSocketOperationsInService(this.getClass().getDeclaredMethods());
        final List<Operation> allRESTOperationsInService = getAllRESTOperationsInService(this.getClass().getDeclaredMethods());
        return Stream.
                concat(allWebSocketOperationsInService.stream(), allRESTOperationsInService.stream()).
                collect(Collectors.toList());
    }


    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     *
     * @param allMethods methods in serviceVerticle
     * @return a list of all operation in service
     */
    private List<Operation> getAllWebSocketOperationsInService(final Method[] allMethods) {
        return Stream.of(allMethods).
                filter(m -> m.isAnnotationPresent(OnWebSocketMessage.class)).
                map(this::mapWebSocketMethod).collect(Collectors.toList());
    }

    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     *
     * @param allMethods methods in serviceVerticle
     * @return a list of all operation in service
     */
    private List<Operation> getAllRESTOperationsInService(final Method[] allMethods) {
        return Stream.of(allMethods).
                filter(m -> m.isAnnotationPresent(Path.class)).
                map(this::mapRESTtMethod).collect(Collectors.toList());
    }

    private Operation mapWebSocketMethod(Method method) {
        final OnWebSocketMessage path = method.getDeclaredAnnotation(OnWebSocketMessage.class);
        // TODO remove Produces and Consumes?
        final Produces produces = method.getDeclaredAnnotation(Produces.class);
        final Consumes consumes = method.getDeclaredAnnotation(Consumes.class);
        if (path == null)
            throw new MissingResourceException("missing OperationType ", this.getClass().getName(), "");
        final String[] mimeTypes = produces != null ? produces.value() : null;
        final String[] consumeTypes = consumes != null ? consumes.value() : null;
        final String url = ConfigurationUtil.serviceName(getConfig(), this.getClass()).concat(path.value());
        final List<String> parameters = new ArrayList<>();

        parameters.addAll(getWSParameter(method));

        // TODO add service description (description about service itself)!!!
        return new Operation(path.value(), null, url, Type.WEBSOCKET.name(), mimeTypes, consumeTypes, parameters.toArray(new String[parameters.size()]));
    }

    private Operation mapRESTtMethod(Method method) {
        final Path path = method.getDeclaredAnnotation(Path.class);
        // TODO implement POST,... create array of Operations for each annotation
        final Produces produces = method.getDeclaredAnnotation(Produces.class);
        final Consumes consumes = method.getDeclaredAnnotation(Consumes.class);
        final GET get = method.getDeclaredAnnotation(GET.class);
        final POST post = method.getDeclaredAnnotation(POST.class);
        final PUT put = method.getDeclaredAnnotation(PUT.class);
        final DELETE delete = method.getDeclaredAnnotation(DELETE.class);
        if (path == null)
            throw new MissingResourceException("missing OperationType ", this.getClass().getName(), "");
        final String[] mimeTypes = produces != null ? produces.value() : null;
        final String[] consumeTypes = consumes != null ? consumes.value() : null;
        final String url = ConfigurationUtil.serviceName(getConfig(), this.getClass()).concat(path.value());
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
    private List<String> getWSParameter(Method method) {
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
    private List<String> getRESTParameter(Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final List<String> classes = Stream.of(parameterTypes).
                filter(component -> !component.equals(RestHandler.class)).
                map(Class::getName).
                collect(Collectors.toList());
        if (classes.size() > 1)
            throw new IllegalArgumentException("only one parameter is allowed -- the message body -- and/or the WSHandler");
        return classes;
    }


    private void info(Message m) {

        try {
            m.reply(Serializer.serialize(getServiceDescriptor()), new DeliveryOptions().setSendTimeout(10000));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public ServiceInfo getServiceDescriptor() {
        return this.descriptor;
    }


    private JsonObject getConfig() {
        return context != null ? context.config() : new JsonObject();
    }


    // TODO add versioning to service
    protected String getVersion() {
        return null;
    }

}
