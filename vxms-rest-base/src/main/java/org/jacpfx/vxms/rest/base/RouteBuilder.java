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

package org.jacpfx.vxms.rest.base;

import io.vertx.core.http.HttpMethod;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes.RestErrorConsumer;
import org.jacpfx.vxms.rest.base.VxmsRESTRoutes.RestHandlerConsumer;

public class RouteBuilder {
    private final MethodDescriptor decriptor;

    public RouteBuilder(MethodDescriptor decriptor) {
        this.decriptor = decriptor;
    }

    public static RouteBuilder get(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.GET, path, methodReference, consumes, null));
    }

    public static RouteBuilder post(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.POST, path, methodReference, consumes, null));
    }

    public static RouteBuilder put(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.PUT, path, methodReference, consumes, null));
    }

    public static RouteBuilder patch(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.PATCH, path, methodReference, consumes, null));
    }

    public static RouteBuilder delete(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.DELETE, path, methodReference, consumes, null));
    }

    public static RouteBuilder options(String path, RestHandlerConsumer methodReference, String... consumes) {
        return new RouteBuilder(
                new MethodDescriptor(HttpMethod.OPTIONS, path, methodReference, consumes, null));
    }


    public RouteBuilder onError(RestErrorConsumer errorMethod) {
        return new RouteBuilder(
                new MethodDescriptor(
                        decriptor.httpMethod,
                        decriptor.path,
                        decriptor.method,
                        decriptor.consumes,
                        errorMethod));
    }

    protected MethodDescriptor getDescriptor() {
        return this.decriptor;
    }
}
