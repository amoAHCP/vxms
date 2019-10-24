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

package org.jacpfx.vxms.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.services.VxmsEndpoint;


/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 8080)
public class SimpleREST extends VxmsEndpoint {

    @Override
    public void postConstruct(Router router, final Future<Void> startFuture) {
        router.get("/helloGET").handler(getSimpleResponse());
        router.get("/helloGET/:name").handler(getName());
        startFuture.complete();
    }

    private Handler<RoutingContext> getName() {
        return helloGet -> helloGet.response().end("hello World " + helloGet.request().getParam("name"));
    }

    private Handler<RoutingContext> getSimpleResponse() {
        return helloGet -> helloGet.response().end("simple response");
    }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions();
        System.out.println("test");
        SimpleREST t = new SimpleREST();
        Vertx.vertx().deployVerticle(t, options);
    }
}
