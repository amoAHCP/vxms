package org.jacpfx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.services.VxmsEndpoint;


/**
 * Created by Andy Moncsek on 25.01.16.
 */
@ServiceEndpoint(port = 9090)
public class SimpleREST extends VxmsEndpoint {

   public void postConstruct(Router router, final Future<Void> startFuture){
       router.get("/helloGET").handler(helloGet -> helloGet.response().end("simple response"));
       router.get("/helloGET/:name").handler(helloGet -> helloGet.response().end("hello World "+helloGet.request().getParam("name")));
       startFuture.complete();
   }

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(new JsonObject().put("host","localhost"));
        Vertx.vertx().deployVerticle(SimpleREST.class.getName(),options);
    }
}
