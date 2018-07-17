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

package org.jacpfx.vxms.spring;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.jacpfx.vertx.spring.SpringVerticleFactory;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.rest.annotation.OnRestError;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.jacpfx.vxms.spring.beans.HelloWorldBean;
import org.jacpfx.vxms.spring.configuration.SpringConfig;

/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 9090)
@SpringVerticle(springConfig = SpringConfig.class)
public class SimpleSpringRESTStaticInit extends AbstractVerticle {

    public void start(Future<Void> startFuture) {
        SpringVerticleFactory.initSpring(this);
        VxmsEndpoint.start(startFuture,this);
    }

    @Inject
    public HelloWorldBean bean;

    @Path("/helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
        handler.response().blocking().stringResponse(() -> bean.sayHallo()+"  ..... 1").execute();
    }


    @Path("/helloGET/:name")
    @GET
    public void simpleRESTHelloWithParameter(RestHandler handler) {
        handler.response().blocking().stringResponse(() -> {
            final String name = handler.request().param("name");
            return bean.sayHallo(name);
        }).execute();
    }


    @Path("/simpleExceptionHandling/:name")
    @GET
    public void simpleExceptionHandling(RestHandler handler) {
        handler.response().blocking().stringResponse(() -> bean.seyHelloWithException()).execute();
    }

    @OnRestError("/simpleExceptionHandling/:name")
    @GET
    public void simpleExceptionHandlingOnError(Throwable t, RestHandler handler) {
        handler.response().stringResponse((future) -> future.complete("Hi "+handler.request().param("name")+" ::"+t.getMessage())).execute();
    }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions();
        Vertx.vertx().deployVerticle(SimpleSpringRESTStaticInit.class.getName(), options);
    }
}
