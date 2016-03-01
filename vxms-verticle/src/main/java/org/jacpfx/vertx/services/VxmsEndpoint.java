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
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.Operation;
import org.jacpfx.common.ServiceInfo;
import org.jacpfx.common.Type;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.vertx.rest.annotation.EndpointConfig;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.configuration.DefaultEndpointConfiguration;
import org.jacpfx.vertx.rest.configuration.EndpointConfiguration;
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
import java.util.Optional;
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

        initSelfHostedService(startFuture);

        long endTime = System.currentTimeMillis();
        log.info("start time: " + (endTime - startTime) + "ms");


    }

    private EndpointConfiguration getEndpointConfiguration() {
        EndpointConfiguration endpointConfig = null;
        if (getClass().isAnnotationPresent(EndpointConfig.class)) {
            final EndpointConfig annotation = getClass().getAnnotation(EndpointConfig.class);
            final Class<? extends EndpointConfiguration> epConfigClazz = annotation.value();
            try {
                endpointConfig = epConfigClazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return endpointConfig == null ? new DefaultEndpointConfiguration() : endpointConfig;
    }

    private void initSelfHostedService(final Future<Void> startFuture) {
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

            // REST -----------------------------------------
            Router router = Router.router(vertx);

            Optional.of(getEndpointConfiguration()).ifPresent(endpointConfig -> {

                Optional.ofNullable(endpointConfig.corsHandler()).ifPresent(corsHandler -> router.route().handler(corsHandler));

                Optional.ofNullable(endpointConfig.bodyHandler()).ifPresent(bodyHandler -> router.route().handler(bodyHandler));

                Optional.ofNullable(endpointConfig.cookieHandler()).ifPresent(cookieHandler -> router.route().handler(cookieHandler));

                Optional.ofNullable(endpointConfig.authHandler()).ifPresent(authHandler -> router.route().handler(authHandler));

                Optional.ofNullable(endpointConfig.staticHandler()).ifPresent(staticHandler -> router.route().handler(staticHandler));

            });


            // TODO extract/refactor this code !!!
            Stream.of(this.getClass().getDeclaredMethods()).
                    filter(m -> m.isAnnotationPresent(Path.class)).
                    forEach(restMethod -> {
                        Path path = restMethod.getAnnotation(Path.class);
                        final String sName = ConfigurationUtil.serviceName(getConfig(), service.getClass());
                        // final boolean isRegEx = path.value().contains(REGEX_CHECK);
                        // TODO add annotation with config class to allow individual configuration of BodyHandler, CookieHandler, CORSHandler and session

                        // TODO add session handling
                        /**
                         * // Create a clustered session store using defaults
                         SessionStore store = ClusteredSessionStore.create(vertx);

                         SessionHandler sessionHandler = SessionHandler.create(store);
                         */

                        // TODO currently the OnMethodError is not unique for POST/GET whatever... try to indicate for which operation the OnErrorMethod is!!
                        final Stream<Method> errorMethodStream = getRESTMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnRestError.class));
                        final Optional<Consumes> consumes = Optional.ofNullable(restMethod.isAnnotationPresent(Consumes.class) ? restMethod.getAnnotation(Consumes.class) : null);
                        final Optional<GET> get = Optional.ofNullable(restMethod.isAnnotationPresent(GET.class) ? restMethod.getAnnotation(GET.class) : null);
                        final Optional<POST> post = Optional.ofNullable(restMethod.isAnnotationPresent(POST.class) ? restMethod.getAnnotation(POST.class) : null);
                        final Optional<OPTIONS> options = Optional.ofNullable(restMethod.isAnnotationPresent(OPTIONS.class) ? restMethod.getAnnotation(OPTIONS.class) : null);
                        final Optional<PUT> put = Optional.ofNullable(restMethod.isAnnotationPresent(PUT.class) ? restMethod.getAnnotation(PUT.class) : null);
                        final Optional<DELETE> delete = Optional.ofNullable(restMethod.isAnnotationPresent(DELETE.class) ? restMethod.getAnnotation(DELETE.class) : null);

                        get.ifPresent(g -> {
                            final Optional<Method> errorMethod = errorMethodStream.filter(method ->method.isAnnotationPresent(GET.class)).findFirst();
                            final Route route = router.get(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, errorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });
                        });
                        post.ifPresent(g -> {
                            final Optional<Method> errorMethod = errorMethodStream.filter(method ->method.isAnnotationPresent(POST.class)).findFirst();
                            final Route route = router.post(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, errorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });

                        });
                        options.ifPresent(g -> {
                            final Optional<Method> errorMethod = errorMethodStream.filter(method ->method.isAnnotationPresent(OPTIONS.class)).findFirst();
                            final Route route = router.options(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, errorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });
                        });

                        put.ifPresent(g -> {
                            final Optional<Method> errorMethod = errorMethodStream.filter(method ->method.isAnnotationPresent(PUT.class)).findFirst();
                            final Route route = router.put(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, errorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });
                        });
                        delete.ifPresent(g -> {
                            final Optional<Method> errorMethod = errorMethodStream.filter(method ->method.isAnnotationPresent(OPTIONS.class)).findFirst();
                            final Route route = router.delete(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, errorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });
                        });
                        if (!get.isPresent() && !post.isPresent() && options.isPresent() && !put.isPresent() && delete.isPresent()) {
                            // TODO check for Config provider or fallback
                            final Optional<Method> onErrorMethod = getRESTMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnRestError.class)).findFirst();
                            final Route route = router.route(sName + path.value()).handler(routingContext ->
                                    handleRESTRoutingContext(service, restMethod, onErrorMethod, routingContext));
                            consumes.ifPresent(cs -> {
                                if (cs.value().length > 0) {
                                    Stream.of(cs.value()).forEach(mime -> route.consumes(mime));
                                }
                            });
                        }

                    });

            Optional.of(getEndpointConfiguration()).ifPresent(endpointConfig -> {
                endpointConfig.customRouteConfiguration(vertx, router);
            });

            server.requestHandler(router::accept).listen(status -> {
                if (status.succeeded()) {
                    log("started on PORT: " + port + " host: " + host);


                    startFuture.complete();
                    return;
                }
                startFuture.fail(status.cause());
            });
        }
    }

    private void handleRESTRoutingContext(Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        try {
            final Object[] parameters = ReflectionUtil.invokeRESTParameters(
                    routingContext,
                    restMethod,
                    vertx,
                    null,
                    throwable -> handleRestError(service, onErrorMethod, routingContext, throwable));
            ReflectionUtil.genericMethodInvocation(
                    restMethod,
                    () -> parameters, service);
        } catch (Throwable throwable) {
            handleRestError(service, onErrorMethod, routingContext, throwable);

        }
    }

    private void handleRestError(Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
        if (onErrorMethod.isPresent()) {
            try {
                ReflectionUtil.genericMethodInvocation(onErrorMethod.get(), () -> ReflectionUtil.invokeRESTParameters(routingContext, onErrorMethod.get(), vertx, throwable, null), service);
            } catch (Throwable throwable1) {
                routingContext.fail(throwable1);
                throwable1.printStackTrace();
            }
        } else {
            routingContext.fail(throwable);
            throwable.printStackTrace();
        }
    }

    private static List<Method> getRESTMethods(Object service, String sName) {
        final String methodName = sName;
        final Method[] declaredMethods = service.getClass().getDeclaredMethods();
        return Stream.of(declaredMethods).
                filter(method -> filterRESTMethods(method, methodName)).collect(Collectors.toList());
    }

    private static boolean filterRESTMethods(final Method method, final String methodName) {
        if (method.isAnnotationPresent(Path.class) && method.getAnnotation(Path.class).value().equalsIgnoreCase(methodName))
            return true;
        return method.isAnnotationPresent(OnRestError.class) && method.getAnnotation(OnRestError.class).value().equalsIgnoreCase(methodName);

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
