[![Build Status](https://travis-ci.org/amoAHCP/vxms.svg?branch=master)](https://travis-ci.org/amoAHCP/vxms)

# vxms
Vxms is a modular micro service framework, based 100% on Vert.x 3. While Vert.x is a totally unopinionated framework/toolkit, vxms helps the developer to create (micro) services typically using REST and/or events. 
Currently vxms consists of 1 base module and 4 extension modules, helping the developer to write Jax-RX like REST services, WebSocket endpoints and handling service registration/discovery using etcd. Since the *core module* is using Java SPIs to handle REST, WebSocket and service registration you can adopt the API easily for your needs.
Vxms only uses Vert.x-core and Vert.x-web extension as dependencies and any other Vert.x extension will work in vxms out of the box.
    

## vxms-rest example

```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {

   
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
      String name =   handler.request().param("name");
      handler.
                      response().
                      stringResponse((response)->
                              response.complete("hello World "+name)). // complete non-blocking response
                      timeout(2000). // timeout for stringResponse handling. If timeout is reached, error handling will be executed
                      onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                      onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                      httpErrorCode(HttpResponseStatus.BAD_REQUEST). // http error code in case of onFailureRespond will be executed
                      retry(3). // amount of retries before onFailureRespond will be executed
                      closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                      execute(); // execute non blocking
    }
    
    
    @Path("/helloBlocking/:name")
    @GET
    public void simpleBlocking(RestHandler handler) {
       String name =   handler.request().param("name");
       handler.
                       response().
                       blocking().
                       stringResponse(()->{
                            String val = blockingCall();
                            return val+ "hello World "+name;
                       }). // complete blocking response
                       timeout(15000). // timeout for stringResponse handling. If timeout is reached, error handling will be executed
                       onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                       onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                       httpErrorCode(HttpResponseStatus.BAD_REQUEST). // http error code in case of onFailureRespond will be executed
                       retry(3). // amount of retries before onFailureRespond will be executed
                       closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                       execute(); // execute non blocking
          
     }
     
     @Path("/helloEventbus/:name")
     @GET
     public void simpleEventbusCall(RestHandler handler) {
        String name =   handler.request().param("name");
        handler.
                        eventBusRequest().
                        send("/consumer.hello", name). // send message to eventbus consumer
                        mapToStringResponse((handler, response)->
                                     response.complete(handler.result().body()). // on message response, map message reply value to rest response                        ). // complete non-blocking response
                        timeout(5000). // timeout for mapToStringResponse handling. If timeout is reached, error handling will be executed
                        onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                        onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                        httpErrorCode(HttpResponseStatus.BAD_REQUEST). // http error code in case of onFailureRespond will be executed
                        retry(3). // amount of retries before onFailureRespond will be executed
                        closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                        execute(); // execute non blocking
               
          }
     
     private String blockingCall(){
        // block
        return "xyz";
     } 

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(RESTExample.class.getName());
    }
}
``` 

## vxms-eventbus example

```java
@ServiceEndpoint
public class EventbusExample extends VxmsEndpoint {

   
    @Consume("/consumer.hello")
    public void simpleNonBlocking(EventbusHandler handler) {
      String name =   handler.request().body();
      handler.
                      response().
                      stringResponse((response)->
                              response.complete("hello World "+name)). // complete non-blocking response
                      timeout(2000). // timeout for stringResponse handling. If timeout is reached, error handling will be executed
                      onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                      onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                      httpErrorCode(HttpResponseStatus.BAD_REQUEST). // http error code in case of onFailureRespond will be executed
                      retry(3). // amount of retries before onFailureRespond will be executed
                      closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                      execute(); // execute non blocking
    }
    
   
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(EventbusExample.class.getName());
    }
}
```

## vxms-core
The vxms-core module contains the *abstract VxmsEndpoint* class which extends *AbstractVerticle" class (from Vert.x). This class must be extended by every vxms service, to be able to use all other modules. The core module didn'failure add much extra functionality, but it provides some convenience function over a plain Verticle. A minimal vxms (core) endpoint looks like this:
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

### What you get, using this minimal configuration:
1. starting this VxmsEndpoint will create a http endpoint (on port 8080, listening on all network interfaces, with Body- and CookieHandler enabled) and providing the reference to the Vert.x-web Router instance (on *postConstruct*). You can use the *io.vertx.ext.web.Router* class in vxms exactly the same way as described in the Vert.x-Web tutorial [here:](http://vertx.io/docs/vertx-web/java/#_routing_by_http_method) 
2. ability to configure the http endpoint by using the *@ServiceEndpoint* annotation and/or default the Vert.x configuration.


Using the *@ServiceEndpoint* annotation you can specify following:
- the port number
- the host configuration
- the name of the service
- the context root of your service
- the endpoint options, using io.vertx.core.http.HttpServerOptions
The port, host and name configuration can also be specified through Vert.x json configuration using the same names.

### specify the optional EndpointConfiguration
```java
   @ServiceEndpoint
   @EndpointConfig(CustomEndpointConfiguration.class)
   public class SimpleService extends VxmsEndpoint {
    ...
   } 
``` 

The *@EndpointConfig* annotation takes a class, implementing the *EndpointConfiguration* interface, as value. This interface defines some default methods that can be overwritten to customize your Service. Following configuration can be done (methods to overwrite):
- *void corsHandler(Router router)* : define a corse handler for your service
- *void bodyHandler(Router router)* : set the body handler; a body handler is always set by default, if you don'failure want this overwrite this method with an empty implementation
- *void cookieHandler(Router router)*: set the cookie handler; a cookie handler is always set by default, if you don'failure want this overwrite this method with an empty implementation
- *void staticHandler(Router router)*: specify a static content handler
- *void sessionHandler(Vertx vertx, Router router)*: specify the session handler
- *void customRouteConfiguration(Vertx vertx, Router router, boolean secure, String host, int port)*: define some custom route configurations like security for your service


