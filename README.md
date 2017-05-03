[![Build Status](https://travis-ci.org/amoAHCP/vxms.svg?branch=master)](https://travis-ci.org/amoAHCP/vxms)

# vxms
Vxms is a modular micro service framework, based 100% on Vert.x 3. While Vert.x is a totally un opinionated framework/toolkit, vxms helps the developer to create (micro) services typically using REST and/or events. 
Currently vxms consists of 1 base module and 4 extension modules, helping the developer to write Jax-RS like REST services, EventBus endpoints and handling service registration/discovery using etcd. Since the *core module* is using Java SPIs to handle REST, EventBus and service registration you can adopt the API easily for your needs.
Vxms only uses Vert.x-core and Vert.x-web extension as dependencies and any other Vert.x extension will work in vxms out of the box.
    
## maven dependencies

### vxms-core
```xml
 <dependency>
      <groupId>org.jacpfx</groupId>
      <artifactId>vxms-core</artifactId>
      <version>1.0-SNAPSHOT</version>
 </dependency>
```   
### vxms-rest
```xml   
  <dependency>
       <groupId>org.jacpfx</groupId>
       <artifactId>vxms-rest</artifactId>
       <version>1.0-SNAPSHOT</version>
  </dependency>
```   
### vxms-event bus
```xml
 <dependency>
        <groupId>org.jacpfx</groupId>
        <artifactId>vxms-event</artifactId>
        <version>1.0-SNAPSHOT</version>
  </dependency>
```   
  ### snapshot repository
```xml
 <repositories>
        <repository>
            <id>sonatype-nexus-snapshots</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        </repository>
    </repositories>
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
                        send("/onSuccess.hello", name). // send message to eventbus onSuccess
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

   
    @Consume("/onSuccess.hello")
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
