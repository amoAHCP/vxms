package org.jacpfx.vertx.rest.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.response.RestHandler;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 09.03.16.
 */
public class RESTInitializer {


    public static final String ROOT = "/";

    /**
     * initialize default REST implementation for vxms
     *
     * @param vertx   the Vert.x instance
     * @param router  the Router instance
     * @param config  the Configuration provided by the Vert.x instance
     * @param service the Vxms service object itself
     */
    public static void initRESTHandler(Vertx vertx, Router router, JsonObject config, Object service) {
        Stream.of(service.getClass().getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(Path.class)).
                forEach(restMethod -> initRestMethod(vertx, router, config, service, restMethod));
    }

    /**
     * Initialize a specific REST method from Service
     * @param vertx The Vertx instance
     * @param router The Router object
     * @param config The Vertx configuration
     * @param service The Service itself
     * @param restMethod the REST Method
     */
    public static void initRestMethod(Vertx vertx, Router router, JsonObject config, Object service, Method restMethod) {
        final Path path = restMethod.getAnnotation(Path.class);
        final Stream<Method> errorMethodStream = getRESTMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnRestError.class));
        final Optional<Consumes> consumes = Optional.ofNullable(restMethod.isAnnotationPresent(Consumes.class) ? restMethod.getAnnotation(Consumes.class) : null);
        final Optional<GET> get = Optional.ofNullable(restMethod.isAnnotationPresent(GET.class) ? restMethod.getAnnotation(GET.class) : null);
        final Optional<POST> post = Optional.ofNullable(restMethod.isAnnotationPresent(POST.class) ? restMethod.getAnnotation(POST.class) : null);
        final Optional<OPTIONS> options = Optional.ofNullable(restMethod.isAnnotationPresent(OPTIONS.class) ? restMethod.getAnnotation(OPTIONS.class) : null);
        final Optional<PUT> put = Optional.ofNullable(restMethod.isAnnotationPresent(PUT.class) ? restMethod.getAnnotation(PUT.class) : null);
        final Optional<DELETE> delete = Optional.ofNullable(restMethod.isAnnotationPresent(DELETE.class) ? restMethod.getAnnotation(DELETE.class) : null);

        get.ifPresent(g -> initHttpGet(vertx, router, service, restMethod, path, errorMethodStream, consumes));
        post.ifPresent(g -> initHttpPost(vertx, router, service, restMethod, path, errorMethodStream, consumes));
        options.ifPresent(g -> initHttpOptions(vertx, router, service, restMethod, path, errorMethodStream, consumes));
        put.ifPresent(g -> initHttpPut(vertx, router, service, restMethod, path, errorMethodStream, consumes));
        delete.ifPresent(g -> initHttpDelete(vertx, router, service, restMethod, path, errorMethodStream, consumes));

        if (!get.isPresent() && !post.isPresent() && !options.isPresent() && !put.isPresent() && !delete.isPresent()) {
            initHttpAll(vertx, router, service, restMethod, path, errorMethodStream, consumes);
        }
    }

    private static void initHttpOperation(String methodId, Vertx vertx, Object service, Method restMethod, Route route, Stream<Method> errorMethodStream, Optional<Consumes> consumes, Class<? extends Annotation> httpAnnotation) {
        final Optional<Method> errorMethod = errorMethodStream.filter(method -> method.isAnnotationPresent(httpAnnotation)).findFirst();
        initHttpRoute(methodId, vertx, service, restMethod, consumes, errorMethod, route);
    }

    private static void initHttpAll(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Optional<Method> errorMethod = errorMethodStream.findFirst();
        final Route route = router.route(cleanPath(path.value()));
        final String methodId = path.value() + "ALL";
        initHttpRoute(methodId, vertx, service, restMethod, consumes, errorMethod, route);
    }

    private static void initHttpDelete(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.delete(cleanPath(path.value()));
        final String methodId = path.value() + DELETE.class.getName();
        initHttpOperation(methodId, vertx, service, restMethod, route, errorMethodStream, consumes, DELETE.class);
    }

    private static void initHttpPut(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.put(cleanPath(path.value()));
        final String methodId = path.value() + PUT.class.getName();
        initHttpOperation(methodId, vertx, service, restMethod, route, errorMethodStream, consumes, PUT.class);
    }

    private static void initHttpOptions(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.options(cleanPath(path.value()));
        final String methodId = path.value() + OPTIONS.class.getName();
        initHttpOperation(methodId, vertx, service, restMethod, route, errorMethodStream, consumes, OPTIONS.class);
    }

    private static void initHttpPost(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.post(cleanPath(path.value()));
        final String methodId = path.value() + POST.class.getName();
        initHttpOperation(methodId, vertx, service, restMethod, route, errorMethodStream, consumes, POST.class);
    }

    protected static void initHttpGet(Vertx vertx, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.get(cleanPath(path.value()));
        final String methodId = path.value() + GET.class.getName();
        initHttpOperation(methodId, vertx, service, restMethod, route, errorMethodStream, consumes, GET.class);
    }

    private static void initHttpRoute(String methodId, Vertx vertx, Object service, Method restMethod, Optional<Consumes> consumes, Optional<Method> errorMethod, Route route) {
        route.handler(routingContext ->
                handleRESTRoutingContext(methodId, vertx, service, restMethod, errorMethod, routingContext));
        updateHttpConsumes(consumes, route);
    }

    private static void updateHttpConsumes(Optional<Consumes> consumes, Route route) {
        consumes.ifPresent(cs -> {
            if (cs.value().length > 0) {
                Stream.of(cs.value()).forEach(route::consumes);
            }
        });
    }


    private static List<Method> getRESTMethods(Object service, String sName) {
        final String methodName = sName;
        final Method[] declaredMethods = service.getClass().getDeclaredMethods();
        return Stream.
                of(declaredMethods).
                filter(method -> filterRESTMethods(method, methodName)).
                collect(Collectors.toList());
    }

    private static boolean filterRESTMethods(final Method method, final String methodName) {
        if (method.isAnnotationPresent(Path.class) && method.getAnnotation(Path.class).value().equalsIgnoreCase(methodName))
            return true;
        return method.isAnnotationPresent(OnRestError.class) && method.getAnnotation(OnRestError.class).value().equalsIgnoreCase(methodName);

    }

    private static void handleRESTRoutingContext(String methodId, Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        try {
            final Object[] parameters = getInvocationParameters(methodId, vertx, service, restMethod, onErrorMethod, routingContext);
            ReflectionUtil.genericMethodInvocation(restMethod, () -> parameters, service);
        } catch (Throwable throwable) {
            handleRestError(methodId + "ERROR", vertx, service, onErrorMethod, routingContext, throwable);
        }
    }

    private static Object[] getInvocationParameters(String methodId, Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        final Consumer<Throwable> throwableConsumer = throwable -> handleRestError(methodId + "ERROR", vertx, service, onErrorMethod, routingContext, throwable);
        return ReflectionUtil.invokeRESTParameters(
                routingContext,
                restMethod,
                null,
                new RestHandler(methodId, routingContext, vertx, null, throwableConsumer));
    }

    private static void handleRestError(String methodId, Vertx vertx, Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
        if (onErrorMethod.isPresent()) {
            invokeOnErrorMethod(methodId, vertx, service, onErrorMethod, routingContext, throwable);
        } else {
            failRequest(routingContext, throwable);
        }
    }


    private static void invokeOnErrorMethod(String methodId, Vertx vertx, Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
        onErrorMethod.ifPresent(errorMethod -> {
            try {
                ReflectionUtil.genericMethodInvocation(errorMethod, () -> ReflectionUtil.invokeRESTParameters(routingContext, errorMethod, throwable, new RestHandler(methodId, routingContext, vertx, throwable, null)), service);
            } catch (Throwable t) {
                failRequest(routingContext, t);
            }

        });
    }

    private static void failRequest(RoutingContext routingContext, Throwable throwable) {
        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).setStatusMessage(throwable.getMessage()).end();
        throwable.printStackTrace();
    }

    private static String cleanPath(String path) {
        return path.startsWith(ROOT) ? path : ROOT + path;
    }
}
