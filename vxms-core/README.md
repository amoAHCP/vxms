# vxms core module
The vxms-core module contains the *abstract VxmsEndpoint* class which extends *AbstractVerticle" class (from Vert.x). 
This class must be extended by every vxms service, to be able to use all other modules. The core module didn't add much extra functionality, but it provides some convenience function (especially configuration wise ) over a plain Verticle. 
A minimal vxms (core) endpoint looks like this:
```java
   @ServiceEndpoint
   public class SimpleService extends VxmsEndpoint {
   
      public void postConstruct(Router router, final Future<Void> startFuture){
             router.get("/hello").handler(helloGet -> helloGet.response().end("simple response"));
      }
      
      public static void main(String[] args) {
              Vertx.vertx().deployVerticle(SimpleREST.class.getName());
       }
   } 
```

Since Vxms 1.1 you can use a static initializer to activate Vxms on a plain Vert.x *Verticle*. Keep in mind, that the initialization must be the the last step in you configuration and that the verticle must be annotated with *@ServiceEndpoint*

```java
   @ServiceEndpoint
   public class SimpleService extends AbstractVerticle {
  
      @Override
      public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
        VxmsEndpoint.start(startFuture, this);
      }
   
      public void postConstruct(Router router, final Future<Void> startFuture){
             router.get("/hello").handler(helloGet -> helloGet.response().end("simple response"));
      }
      
      public static void main(String[] args) {
              Vertx.vertx().deployVerticle(SimpleREST.class.getName());
       }
   } 
``` 

### What you get, using this minimal configuration:
1. starting this VxmsEndpoint will create a http endpoint (on port 8080, listening on all network interfaces, with Body- and CookieHandler enabled) 
and providing the reference to the Vert.x-web Router instance (on *postConstruct*). You can use the *io.vertx.ext.web.Router* class in vxms exactly the same way as 
described in the Vert.x-Web tutorial [here:](http://vertx.io/docs/vertx-web/java/#_routing_by_http_method) 
2. ability to configure the http endpoint by using the *@ServiceEndpoint* annotation and/or default the Vert.x configuration.


Using the *@ServiceEndpoint* annotation you can specify following:
- the port number
- the host configuration
- the name of the service
- the context root of your service
- the routerConf options, using the org.jacpfx.vxms.common.configuration.RouterConfiguration interface
- the serverOptions, using the org.jacpfx.vxms.common.CustomServerOptions to provide custom io.vertx.core.http.HttpServerOptions configuration
All properties can be configured using the annotation or the Vert.x propery file configuration or using environmental variables

If you are working only with the event-bus module no additional http port is needed. In this case you can specify the port number with *0* and no http Endpoint will be created.

### specify the optional Router configuration
```java
   @ServiceEndpoint(routerConf=DefaultRouterConfiguration.class)
   public class SimpleService extends VxmsEndpoint {
    ...
   } 
``` 

The property *routerConf* takes a class, implementing the *RouterConfiguration* interface, as value. This interface defines some default methods that can be overwritten to customize your Service. Following configuration can be done (methods to overwrite):
- *void corsHandler(Router router)* : define a corse handler for your service
- *void bodyHandler(Router router)* : set the body handler; a body handler is always set by default, if you don'failure want this overwrite this method with an empty implementation
- *void cookieHandler(Router router)*: set the cookie handler; a cookie handler is always set by default, if you don'failure want this overwrite this method with an empty implementation
- *void staticHandler(Router router)*: specify a static content handler
- *void sessionHandler(Vertx vertx, Router router)*: specify the session handler
- *void customRouteConfiguration(Vertx vertx, Router router, boolean secure, String host, int port)*: define some custom route configurations like security for your service

### specify the optional HTTP Server options
```java
   @ServiceEndpoint(serverOptions=DefaultServerOptions.class)
   public class SimpleService extends VxmsEndpoint {
    ...
   } 
``` 
The property *serverOptions* specifies a class, implementing the org.jacpfx.vxms.common.CustomServerOptions interface. Her you can define specific io.vertx.core.http.HttpServerOption configurations


### external configurations
The vxms core module provides some properties, that you can define inside the code or provide it as external configuration. Those configurations can be done the Vert.x way, by defining a json and pass it at the command line "may.jar -config myConfig.json" or using environmental variables. When using environmental variables, the convention is to use capital letters for the property names. Following properties can be overwritten externally:
 
  
 | property name |  description                        |  default |
 |--- |---|---|
 | serviceName  |  the name/identifier of the service  | --- 
 | port          |  the port number to bind http socket |  8080 
 | host          |  the host name/interface to bind to  | 0.0.0.0 
 | contextRoot  |  the context-route for your service  | "/" 
 | serverOptions  |  the HttpServer options, provide a fully qualified class name implementing org.jacpfx.vxms.common.CustomServerOptions  | org.jacpfx.vxms.common.DefaultServerOptions
 | routerConf  |  the Vert.x Router configuration , provide a fully qualified class name implementing org.jacpfx.vxms.common.configuration.RouterConfiguration  | org.jacpfx.vxms.common.configuration.DefaultRouterConfiguration
 