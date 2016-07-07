package org.jacpfx.vertx.rest.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.rest.annotation.EndpointConfig;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.configuration.DefaultEndpointConfiguration;
import org.jacpfx.vertx.rest.configuration.EndpointConfiguration;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.vertx.services.util.ReflectionUtil;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 09.03.16.
 */
public class RESTInitializer {



    public static void initRESTHandler(Vertx vertx, Router router, JsonObject config, Object service) {
        final EndpointConfiguration endpointConfiguration = getEndpointConfiguration(service);

        initEndoitConfiguration(endpointConfiguration,vertx, router);

        Stream.of(service.getClass().getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(Path.class)).
                forEach(restMethod -> initRestMethod(vertx, router, config, service, restMethod));

        postEndoitConfiguration(endpointConfiguration, router);
    }

    protected static void initEndoitConfiguration(EndpointConfiguration endpointConfiguration, Vertx vertx, Router router) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> {

            endpointConfig.corsHandler(router);

            endpointConfig.bodyHandler(router);

            endpointConfig.cookieHandler(router);

            endpointConfig.sessionHandler(vertx, router);

            endpointConfig.customRouteConfiguration(vertx, router);
        });
    }

    protected static void postEndoitConfiguration(EndpointConfiguration endpointConfiguration, Router router) {
        Optional.of(endpointConfiguration).ifPresent(endpointConfig -> endpointConfig.staticHandler(router));
    }

    protected static void initRestMethod(Vertx vertx, Router router, JsonObject config, Object service, Method restMethod) {
        final Path path = restMethod.getAnnotation(Path.class);
        final String sName = ConfigurationUtil.serviceName(config, service.getClass());

        final Stream<Method> errorMethodStream = getRESTMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnRestError.class));
        final Optional<Consumes> consumes = Optional.ofNullable(restMethod.isAnnotationPresent(Consumes.class) ? restMethod.getAnnotation(Consumes.class) : null);
        final Optional<GET> get = Optional.ofNullable(restMethod.isAnnotationPresent(GET.class) ? restMethod.getAnnotation(GET.class) : null);
        final Optional<POST> post = Optional.ofNullable(restMethod.isAnnotationPresent(POST.class) ? restMethod.getAnnotation(POST.class) : null);
        final Optional<OPTIONS> options = Optional.ofNullable(restMethod.isAnnotationPresent(OPTIONS.class) ? restMethod.getAnnotation(OPTIONS.class) : null);
        final Optional<PUT> put = Optional.ofNullable(restMethod.isAnnotationPresent(PUT.class) ? restMethod.getAnnotation(PUT.class) : null);
        final Optional<DELETE> delete = Optional.ofNullable(restMethod.isAnnotationPresent(DELETE.class) ? restMethod.getAnnotation(DELETE.class) : null);

        get.ifPresent(g -> initHttpGet(vertx, router, service, restMethod, path, sName, errorMethodStream, consumes));
        post.ifPresent(g -> initHttpPost(vertx, router, service, restMethod, path, sName, errorMethodStream, consumes));
        options.ifPresent(g -> initHttpOptions(vertx, router, service, restMethod, path, sName, errorMethodStream, consumes));
        put.ifPresent(g -> initHttpPut(vertx, router, service, restMethod, path, sName, errorMethodStream, consumes));
        delete.ifPresent(g -> initHttpDelete(vertx, router, service, restMethod, path, sName, errorMethodStream, consumes));

        if (!get.isPresent() && !post.isPresent() && options.isPresent() && !put.isPresent() && delete.isPresent()) {
            // TODO check for Config provider or fallback
            initHttpAll(vertx, router, service, restMethod, path, sName, errorMethodStream,consumes);
        }
    }

    protected static void initHttpOperation(Vertx vertx, Object service, Method restMethod, Route route, Stream<Method> errorMethodStream, Optional<Consumes> consumes, Class<? extends Annotation> httpAnnotation) {
        final Optional<Method> errorMethod = errorMethodStream.filter(method -> method.isAnnotationPresent(httpAnnotation)).findFirst();
        initHttpRoute(vertx, service, restMethod, consumes, errorMethod, route);
    }

    protected static void initHttpAll(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream,Optional<Consumes> consumes) {
        final Optional<Method> errorMethod = errorMethodStream.findFirst();
        final Route route = router.route(sName + path.value());
        initHttpRoute(vertx, service, restMethod, consumes, errorMethod, route);
    }

    protected static void initHttpDelete(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.delete(sName + path.value());
        initHttpOperation(vertx,service,restMethod,route,errorMethodStream,consumes,DELETE.class);
    }

    protected static void initHttpPut(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.put(sName + path.value());
        initHttpOperation(vertx,service,restMethod,route,errorMethodStream,consumes,PUT.class);
    }

    protected static void initHttpOptions(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.options(sName + path.value());
        initHttpOperation(vertx,service,restMethod,route,errorMethodStream,consumes,OPTIONS.class);
    }

    protected static void initHttpPost(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.post(sName + path.value());
        initHttpOperation(vertx,service,restMethod,route,errorMethodStream,consumes,POST.class);
    }

    protected static void initHttpGet(Vertx vertx, Router router, Object service, Method restMethod, Path path, String sName, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.get(sName + path.value());
        initHttpOperation(vertx,service,restMethod,route,errorMethodStream,consumes,GET.class);
    }

    private static void initHttpRoute(Vertx vertx, Object service, Method restMethod, Optional<Consumes> consumes, Optional<Method> errorMethod, Route route) {
        route.handler(routingContext ->
                handleRESTRoutingContext(vertx, service, restMethod, errorMethod, routingContext));
        consumes.ifPresent(cs -> {
            if (cs.value().length > 0) {
                Stream.of(cs.value()).forEach(route::consumes);
            }
        });
    }


    private static EndpointConfiguration getEndpointConfiguration(Object service) {
        EndpointConfiguration endpointConfig = null;
        if (service.getClass().isAnnotationPresent(EndpointConfig.class)) {
            final EndpointConfig annotation = service.getClass().getAnnotation(EndpointConfig.class);
            final Class<? extends EndpointConfiguration> epConfigClazz = annotation.value();
            try {
                endpointConfig = epConfigClazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return endpointConfig == null ? new DefaultEndpointConfiguration() : endpointConfig;
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

    private static void handleRESTRoutingContext(Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        try {
            final Object[] parameters = getInvocationParameters(vertx, service, restMethod, onErrorMethod, routingContext);
            ReflectionUtil.genericMethodInvocation(restMethod,() -> parameters, service);
        } catch (Throwable throwable) {
            handleRestError(vertx, service, onErrorMethod, routingContext, throwable);

        }
    }

    private static Object[] getInvocationParameters(Vertx vertx, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        return ReflectionUtil.invokeRESTParameters(
                        routingContext,
                        restMethod,
                        vertx,
                        null,
                        throwable -> handleRestError(vertx, service, onErrorMethod, routingContext, throwable));
    }

    private static void handleRestError(Vertx vertx, Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
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
}
