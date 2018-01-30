[![Build Status](https://travis-ci.org/amoAHCP/vxms.svg?branch=master)](https://travis-ci.org/amoAHCP/vxms)

# vxms
Vxms is a modular micro-service framework, based 100% on Vert.x 3. While Vert.x is a totally un opinionated framework/toolkit, Vxms helps the developer to create REST/event based (micro) services with a clear, uniform and easy to use fluent-API. 
Since Vxms is extending Vert.x you can still use all capabilities of Vert.x and mix it with Vxms wherever you like/need. The intention for Vxms was to create a framework on top of the powerful Vert.x framework, allowing developers to quickly create services 
with the focus on writeability, readability and resilience. 
Basically most of todays service endpoints need to handle requests, process data and be aware of the error handling. This is the focus of Vxms. Vert.x is very powerful but many developers still struggling with the reactive style and the callback handling in Vert.x, 
this is the keypoint of Vxms which provides an easy to use API for "everyday" endpoint development,
Currently vxms consists of 1 base module and 2 extension modules, helping the developer to write Jax-RS like REST services and EventBus endpoints. The *core module* is using a Java SPIs to include the REST and EventBus modules, so you can adopt the API easily for your needs.
Vxms only uses Vert.x-core and Vert.x-web extension as dependencies and any other Vert.x extension will work in vxms out of the box.
    
## maven dependencies

### vxms-core  [link](https://github.com/amoAHCP/vxms/tree/master/vxms-core)
```xml
 <dependency>
      <groupId>org.jacpfx</groupId>
      <artifactId>vxms-core</artifactId>
      <version>1.1-RC2</version>
 </dependency>
```   
### vxms-rest  [link](https://github.com/amoAHCP/vxms/tree/master/vxms-rest)
```xml
  <dependency>
       <groupId>org.jacpfx</groupId>
       <artifactId>vxms-rest</artifactId>
       <version>1.1-RC2</version>
  </dependency>
```   
### vxms-event bus  [link](https://github.com/amoAHCP/vxms/tree/master/vxms-event)
```xml
 <dependency>
        <groupId>org.jacpfx</groupId>
        <artifactId>vxms-event</artifactId>
        <version>1.1-RC2</version>
  </dependency>
```   

### vxms-k8s-discovery [link](https://github.com/amoAHCP/vxms/tree/master/vxms-k8sdiscovery)
```xml
 <dependency>
        <groupId>org.jacpfx</groupId>
        <artifactId>vxms-k8sdiscovery</artifactId>
        <version>1.1-RC2</version>
  </dependency>
```   


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
    
     @Path("/helloChain/:name")
     @GET
     public void simpleNonBlockingChain(RestHandler handler) {
       String name =  handler.request().param("name");
       handler.
                       response().
                       <Integer>supply((future) -> future.complete(getAge())). // start the chain by supplying a value (an Integer)
                       <Customer>andThen((value, future) -> future.complete(new Customer(value + 1 + "", name))). // take the value (the Integer) from supply and return an other type (the Customer)
                       mapToStringResponse((customer, response)->
                               response.complete("hello World "+customer.getName())). // get the return-value from the last chain step and map it to a string-response and complete non-blocking response
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
                       delay(1000). // delay between retries
                       closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                       execute(); // execute non blocking
          
     }
     
     @Path("/helloEventbus/:name")
     @GET
     public void simpleEventbusCall(RestHandler handler) {
        String name =   handler.request().param("name");
        handler.
                        eventBusRequest().
                        send("/hello", name). // send message to eventbus onSuccess
                        mapToStringResponse((message, response)->
                                     response.complete(message.result().body()). // on message response, map message result value to the rest response                        ). // complete non-blocking response
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

   
    @Consume("/hello")
    public void simpleNonBlocking(EventbusHandler handler) {
      String name =   handler.request().body();
      handler.
                      response().
                      stringResponse((response)->
                              response.complete("hello World "+name)). // complete non-blocking response
                      timeout(2000). // timeout for stringResponse handling. If timeout is reached, error handling will be executed
                      onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                      onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                      retry(3). // amount of retries before onFailureRespond will be executed
                      closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                      execute(); // execute non blocking
    }
    
     @Consume("/helloChain")
     public void simpleNonBlocking(EventbusHandler handler) {
       String name =   handler.request().body();
       handler.
                      response().
                      <Integer>supply((future) -> getAge()). // start the chain by supplying a value (an Integer)
                      <Customer>andThen((value, future) -> future.complete(new Customer(value + 1 + "", name))). // take the value (the Integer) from supply and return an other type (the Customer)
                       mapToStringResponse((cust, response)->
                            response.complete("hello World "+cust.getName())). // get the return-value from the last chain step and map it to a string-response and complete non-blocking response
                       timeout(2000). // timeout for stringResponse handling. If timeout is reached, error handling will be executed
                       onError(error -> LOG(error.getMessage())).  // intermediate error handling, will be executed on each error
                       onFailureRespond((error, future) -> future.complete("error:"+error.getMessage())). // define final error response when (if no retry is defined or all retries are failing)
                       retry(3). // amount of retries before onFailureRespond will be executed
                       closeCircuitBreaker(2000). // time after circuit breaker will be closed again. While opened, onFailureRespond will be executed on request
                       execute(); // execute non blocking
        }
    
   
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(EventbusExample.class.getName());
    }
}
```

## vxms-k8s-discovery example

```java
@ServiceEndpoint(port=8090)
@K8SDiscovery
public class RESTExample extends VxmsEndpoint {

     @ServiceName()
     @WithLabels({
       @WithLabel(name = "name", value = "${read_name}"),
       @WithLabel(name = "version", value = "${read_version}")
     })
     private String read;
   
     @ServiceName()
     @WithLabels({
       @WithLabel(name = "name", value = "${write_name}"),
       @WithLabel(name = "version", value = "${write_version}")
     })
     private String write;
     
     
     ...


    public static void main(String[] args) {
       // this is only for local discovery to bypass Kubernetes in local environments
       DeploymentOptions options =
              new DeploymentOptions()
                  .setInstances(1)
                  .setConfig(
                      new JsonObject()
                          .put("kube.offline", true)
                          .put("local", true)
                          .put("read_name", "vxms-k8s-read")
                          .put("read_version", "1.1-SNAPSHOT")
                          .put("write_name", "vxms-k8s-write")
                          .put("write_version", "1.1-SNAPSHOT")
                          .put("name.vxms-k8s-read.version.1.1-SNAPSHOT", "localhost:7070")
                          .put("name.vxms-k8s-write.version.1.1-SNAPSHOT", "localhost:9090"));
        Vertx.vertx().deployVerticle(RESTExample.class.getName(), options);
    }
}
``` 

## vxms-core example

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

or using plain Verticles with static initializer

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
