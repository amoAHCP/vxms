/*
 * Copyright [2018] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.rest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


import org.jacpfx.vxms.rest.base.MethodDescriptor;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.util.ConfigurationUtil;
import org.jacpfx.vxms.common.util.URIUtil;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes.RestErrorConsumer;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes.RestHandlerConsumer;
import org.jacpfx.vxms.rest.base.response.RestHandler;
import org.jacpfx.vxms.spi.VxmsRoutes;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

/**
 * Created by Andy Moncsek on 09.03.16. Handles initialization of vxms rest module implementation
 */
public class RestRouteInitializer {


    /**
     * initialize default REST implementation for vxms
     *
     * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
     *                   objects per instance
     * @param router     the Router instance
     * @param routes     the routes defined by the developer with the fluent API
     */
    static void initRESTHandler(
            VxmsShared vxmsShared, Router router, VxmsRoutes routes) {
        if (VxmsRESTRoutes.class.isAssignableFrom(routes.getClass())) {
            VxmsRESTRoutes userRoutes = (VxmsRESTRoutes) routes;
            userRoutes
                    .getDescriptors()
                    .forEach(descriptor -> {
                        switch (descriptor.httpMethod.name()) {
                            case "GET" -> initHttpGet(vxmsShared, router, descriptor);
                            case "POST" -> initHttpPost(vxmsShared, router, descriptor);
                            case "PUT" -> initHttpPut(vxmsShared, router, descriptor);
                            case "DELETE" -> initHttpDelete(vxmsShared, router, descriptor);
                            case "OPTIONS" -> initHttpOptions(vxmsShared, router, descriptor);
                            default -> initHttpGet(vxmsShared, router, descriptor);
                        }

                    });
        }
    }

    private static void initHttpOptions(
            VxmsShared vxmsShared, Router router, MethodDescriptor descriptor) {
        final Route route = router.options(URIUtil.cleanPath(descriptor.path));
        final Context context = getContext(vxmsShared);
        final String methodId =
                descriptor.path
                        + HttpMethod.OPTIONS.name()
                        + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, route, descriptor);
    }

    private static void initHttpDelete(
            VxmsShared vxmsShared, Router router, MethodDescriptor descriptor) {
        final Route route = router.delete(URIUtil.cleanPath(descriptor.path));
        final Context context = getContext(vxmsShared);
        final String methodId =
                descriptor.path
                        + HttpMethod.DELETE.name()
                        + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, route, descriptor);
    }

    private static void initHttpPut(
            VxmsShared vxmsShared, Router router, MethodDescriptor descriptor) {
        final Route route = router.put(URIUtil.cleanPath(descriptor.path));
        final Context context = getContext(vxmsShared);
        final String methodId =
                descriptor.path
                        + POST.name()
                        + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, route, descriptor);
    }

    private static void initHttpPost(
            VxmsShared vxmsShared, Router router, MethodDescriptor descriptor) {
        final Route route = router.post(URIUtil.cleanPath(descriptor.path));
        final Context context = getContext(vxmsShared);
        final String methodId =
                descriptor.path
                        + POST.name()
                        + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, route, descriptor);
    }


    protected static void initHttpGet(
            VxmsShared vxmsShared, Router router, MethodDescriptor descriptor) {
        final Route route = router.get(URIUtil.cleanPath(descriptor.path));
        final Context context = getContext(vxmsShared);
        final String methodId =
                descriptor.path
                        + GET.name()
                        + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, route, descriptor);
    }

    private static void initHttpOperation(
            String methodId, VxmsShared vxmsShared, Route route, MethodDescriptor descriptor) {

        initHttpRoute(
                methodId,
                vxmsShared,
                descriptor.method,
                descriptor.consumes,
                descriptor.errorMethod,
                route);
    }

    private static void initHttpRoute(
            String methodId,
            VxmsShared vxmsShared,
            RestHandlerConsumer restMethod,
            String[] consumes,
            RestErrorConsumer errorMethod,
            Route route) {
        route.handler(
                routingContext ->
                        handleRESTRoutingContext(
                                methodId, vxmsShared, restMethod, errorMethod, routingContext));
        updateHttpConsumes(consumes, route);
    }

    private static void handleRESTRoutingContext(
            String methodId,
            VxmsShared vxmsShared,
            RestHandlerConsumer restMethod,
            RestErrorConsumer onErrorMethod,
            RoutingContext routingContext) {
        try {
            final Consumer<Throwable> throwableConsumer =
                    throwable ->
                            handleRestError(
                                    methodId + "ERROR", vxmsShared, onErrorMethod, routingContext, throwable);
            restMethod.accept(
                    new RestHandler(methodId, routingContext, vxmsShared, null, throwableConsumer));
        } catch (Throwable throwable) {
            handleRestError(methodId + "ERROR", vxmsShared, onErrorMethod, routingContext, throwable);
        }
    }

    private static void handleRestError(
            String methodId,
            VxmsShared vxmsShared,
            RestErrorConsumer onErrorMethod,
            RoutingContext routingContext,
            Throwable throwable) {
        if (onErrorMethod != null) {
            invokeOnErrorMethod(methodId, vxmsShared, onErrorMethod, routingContext, throwable);
        } else {
            // TODO add SPI for custom failure handling
            failRequest(routingContext, throwable);
        }
    }

    private static void invokeOnErrorMethod(
            String methodId,
            VxmsShared vxmsShared,
            RestErrorConsumer onErrorMethod,
            RoutingContext routingContext,
            Throwable throwable) {
        Optional.ofNullable(onErrorMethod)
                .ifPresent(
                        errorMethod -> {
                            try {
                                onErrorMethod.accept(
                                        new RestHandler(methodId, routingContext, vxmsShared, throwable, null),
                                        throwable);
                            } catch (Throwable t) {
                                failRequest(routingContext, t);
                            }
                        });
    }

    private static void updateHttpConsumes(String[] consumes, Route route) {
        Optional.ofNullable(consumes).ifPresent(cs -> Stream.of(cs).forEach(route::consumes));
    }


    private static void failRequest(RoutingContext routingContext, Throwable throwable) {
        routingContext
                .response()
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .setStatusMessage(throwable.getMessage())
                .end();
        throwable.printStackTrace();
    }

    private static Context getContext(VxmsShared vxmsShared) {
        final Vertx vertx = vxmsShared.getVertx();
        return vertx.getOrCreateContext();
    }
}
