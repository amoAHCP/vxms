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

import static junit.framework.TestCase.assertEquals;

import io.vertx.core.http.HttpMethod;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.junit.Test;

public class BuilderTest {

  @Test
  public void testAmountGet() {
    VxmsRESTRoutes tmp =
        VxmsRESTRoutes.init()
            .route(RouteBuilder.get("/hello", this::hello))
            .route(RouteBuilder.get("/hello2", this::hello2))
            .route(RouteBuilder.post("/hello", this::hello))
            .route(RouteBuilder.post("/hello2", this::hello2))
            .route(RouteBuilder.options("/hello", this::hello))
            .route(RouteBuilder.options("/hello2", this::hello2))
            .route(RouteBuilder.put("/hello", this::hello))
            .route(RouteBuilder.put("/hello2", this::hello2))
            .route(RouteBuilder.delete("/hello", this::hello2))
            .route(RouteBuilder.delete("/hello2", this::hello2));

    assertEquals(
        2,
        tmp.getDescriptors()
            .stream()
            .filter(method -> method.getHttpMethod().equals(HttpMethod.GET))
            .collect(Collectors.toList())
            .size());
    assertEquals(
        2,
        tmp.getDescriptors()
            .stream()
            .filter(method -> method.getHttpMethod().equals(HttpMethod.POST))
            .collect(Collectors.toList())
            .size());
    assertEquals(
        2,
        tmp.getDescriptors()
            .stream()
            .filter(method -> method.getHttpMethod().equals(HttpMethod.OPTIONS))
            .collect(Collectors.toList())
            .size());
    assertEquals(
        2,
        tmp.getDescriptors()
            .stream()
            .filter(method -> method.getHttpMethod().equals(HttpMethod.PUT))
            .collect(Collectors.toList())
            .size());
    assertEquals(
        2,
        tmp.getDescriptors()
            .stream()
            .filter(method -> method.getHttpMethod().equals(HttpMethod.DELETE))
            .collect(Collectors.toList())
            .size());
  }

  @Path("/hello")
  @GET
  public void hello(RestHandler handler) {
    handler.response().stringResponse((future) -> future.complete("hi")).execute();
  }

  @Path("/hello2")
  @GET
  public void hello2(RestHandler handler) {
    handler.response().stringResponse((future) -> future.complete("hi")).execute();
  }
}
