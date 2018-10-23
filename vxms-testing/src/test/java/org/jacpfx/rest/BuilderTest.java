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

package org.jacpfx.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vxms.rest.VxmsRESTRoutes;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.junit.Test;

public class BuilderTest {

  @Test
  public void testAmountGet() {
    VxmsRESTRoutes tmp = VxmsRESTRoutes.init().
        get("/hello",this::hello).
        get("/hello2",this::hello2).
        post("/hello",this::hello).
        post("/hello2",this::hello2).
        optional("/hello",this::hello).
        optional("/hello2",this::hello2).
        put("/hello",this::hello).
        put("/hello2",this::hello2).
        delete("/hello",this::hello).
        delete("/hello2",this::hello2);

    assertEquals(2,tmp.getGetMapping().size());
    assertEquals(2,tmp.getPostMapping().size());
    assertEquals(2,tmp.getPutMapping().size());
    assertEquals(2,tmp.getOptionalMapping().size());
    assertEquals(2,tmp.getDeleteMapping().size());
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
