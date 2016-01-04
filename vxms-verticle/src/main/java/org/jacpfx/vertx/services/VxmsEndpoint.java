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
import org.jacpfx.common.Operation;
import org.jacpfx.common.ServiceInfo;
import org.jacpfx.common.Type;
import org.jacpfx.common.util.Serializer;
import org.jacpfx.vertx.services.util.ConfigurationUtil;
import org.jacpfx.vertx.websocket.annotation.OnWebSocketMessage;
import org.jacpfx.vertx.websocket.handler.WebSocketInitializer;
import org.jacpfx.vertx.websocket.registry.LocalWebSocketRegistry;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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
        descriptor = createInfoObject(getAllWebSocketOperationsInService(this.getClass().getDeclaredMethods()), port);
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
            webSocketRegistry = initWebSocketRegistryInstance();
            WebSocketInitializer.registerWebSocketHandler(server,vertx,webSocketRegistry,getConfig(),this);
            server.listen();
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





    private ServiceInfo createInfoObject(List<Operation> operations, Integer port) {
        return new ServiceInfo(ConfigurationUtil.serviceName(getConfig(), this.getClass()), null, ConfigurationUtil.getHostName(), null, null, port, operations.toArray(new Operation[operations.size()]));
    }


    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     *
     * @param allMethods methods in serviceVerticle
     * @return a list of all operation in service
     */
    private List<Operation> getAllWebSocketOperationsInService(final Method[] allMethods) {
        return Stream.of(allMethods).parallel().
                filter(m -> m.isAnnotationPresent(OnWebSocketMessage.class)).
                map(this::mapWebSocketMethod).collect(Collectors.toList());
    }

    private Operation mapWebSocketMethod(Method method) {
        final OnWebSocketMessage path = method.getDeclaredAnnotation(OnWebSocketMessage.class);
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
