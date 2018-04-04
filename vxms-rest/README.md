# vxms rest module
The vxms-rest module is an API to handle rest request with vxms. To use this module, the VxmsEndpoint must be enabled either by extending VxmsEndpoint or by using the static initializer (from the core module).
```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {

}
```
 and following dependency added:

```xml   
  <dependency>
       <groupId>org.jacpfx</groupId>
       <artifactId>vxms-rest</artifactId>
       <version>${version}</version>
  </dependency>
``` 
## basic usage
The vxms-rest module provides a non-blocking (with the option of blocking usage), fluent API to build a REST response. 
You can annotate any public method with Jax-RS annotations and add the *org.jacpfx.vxms.rest.response.RestHandler* class to the method signature, to build the response. 
All methods must *not* have a return value, the response will be returned through the "RestHandler" API. **Each request must be ended, either by calling "execute()" or "end()"**, otherwise the http response will not be ended and the event-bus is blocked. 
The idea in Vxms is, to model your response using the fluent API. Vxms provides a best practice of how to define a repose and to handle errors.

```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
       handler.
                 response().
                 stringResponse(response->
	                 response.complete("simple response")).
                 execute();
		}
}
```
By default, all your operations must be non-blocking. If you do blocking calls, vxms behaves like  Vert.x typically do (either you block the event-bus or you need to operate in worker mode). 
The "RestHandler" gives you the ability to create responses for following types: String, byte[] and Object. For an Object response, you must specify an encoder which maps your response Object to either String or byte[].
### byte example
```java
...
    @Path("/helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
         handler.
                 response().
                 byteResponse((response)->
	                 response.complete("simple response".getBytes())).
                 execute();
    }
    ...
```

### object example
```java
...
    @Path("/helloGET")
    @GET
    public void simpleRESTHello(RestHandler handler) {
         handler.
                 response().
                 objectResponse((response)->
	                 response.complete(new MyObject()),new MyEncoder()).
                 execute();
    }
    
    public class MyObject implements Serializable{
        private String value;

        @Override
        public String toString() {
            return "MyObject{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
    
    public class MyEncoder implements Encoder.StringEncoder<MyObject>{

        @Override
        public String encode(MyObject input) {
            return input.toString();
        }
    }
    ...
```
 
## Jax-rs annotations
The Vxms-rest module is using Jax-rs annotations. Following annotations are supported:

- @Path : The path string follows the Vertx conventions (like: /mypath/:id/:name)
- @GET, @POST, @OPTIONAL, @PUT, @DELETE
- @Consumes
- @Produces

## non-blocking rest response
Vert.x is per default a non-blocking framework, so in Vxms non-blocking is the default behaviour too. A response is always created using the "RestHandler" with it's fluent API. 
When creating specific responses (like String, byte[] or Object), you will need to call the *execute()* method, so the complete chain you defined will be executed. 
For all non-blocking calls you must provide a consumer, which gives you the handler to set the result and to complete the execution.
You can use following options to define response handling:

- handler.response().end() && handler.response().end(HttpStatus status) ends the request (if no response will be submitted) --> **Each request must be ended, either by calling "execute()" or "end()"**
- create a simple String response: handler.response().stringResponse((future)->future.complete("my string")).execute();
- create a simple byte[] response:  handler.response().byteResponse((future)->future.complete("my bytes".getBytes())).execute();
- create a simple Object response:  handler.response().objectResponse((response)-> response.complete(new MyObject()),new MyEncoder()).execute();  ... since Object responses must be serialized you will need to define a custom Encoder which serializes the Object either to String or to byte[]. The Encoder class must implement either  Encoder.StringEncoder<MyObject> or  Encoder.ByteEncoder<MyObject> 
- http specific configurations:
  - putHeader(key,val) : add custom headers to the response
  - execute(String contentType): set the content type of the response
  - execute(HttpResponseStatus status): set the http status for the response
  
## blocking rest response
The blocking API works quite similar to the non-blocking API in Vxms. While using the non-blocking API, you are restricted to the max execution times of Vert.x (time how long the event-loop can be blocked), 
when using the blocking API, you are free to handle any long-running tasks (depending on your worker-thread [configuration](http://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html#setInternalBlockingPoolSize-int-)). 
The basic difference in writing blocking responses is, that the mapping methods (mapToString, mapToObject) are using a **supplier**, which means you simply define a return value, rather than submitting your response value. 

### blocking API example

```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
       handler.
                 response().
                 blocking().
                 stringResponse(response-> "simple response").
                 execute();
		}
}
```

To initialize the blocking API you must call *blocking()* on the response handler.

- handler.response().blocking() : initialize the blocking API
- create simple String response: handler.response().blocking().stringResponse(() ->"hello").execute();
- create simple byte response: handler.response().blocking().byteResponse(() ->"hello".getBytes()).execute();
- create simple Object response: handler.response().blocking().objectResponse(() ->new MyObject(),new MyEncoder()).execute();


## using the supply/andThen chain
Since Vxms 1.1 ,the response handler is able to create a chain of executions for a response. 

### non-blocking chain example
```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
       handler.
                 response().
                <Integer>supply(
                              (future) -> future.complete(1)).
                <String>andThen(
                              (value, future) -> future.complete(value + 1 + "")).
                mapToStringResponse(
                              (val, future) -> future.complete(val + " final")).
                execute();
		}
}
```

### blocking chain example
```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
       handler.
                 response().
                 blocking().
                 supply(() -> 1).
                 andThen((value) -> (value + 1 + "").
                 mapToStringResponse((val) -> val + " final").
                 execute();
		}
}
```
This execution chain gives you the freedom to perform as many tasks as you need, before responding to the request. You can add as many *andThen* executions as you want before you must decide the type of response mapping. 
As before you must map the result of your execution chain to *String*, *byte[]* or *Object*. In terms of error handling (see below) following rules are applied:
- if you add a retry definition, it applies to the step (supply or andThan) that fails and not to the complete chain
- if you add a timeout definition, it applies to each step, so be aware of the possible max execution time of your chain when performing many steps


## error handling
Vxms provides many features to handle errors when building the REST response (in blocking and non-blocking mode):
- onError((throwable) -> ....)... : this is an intermediate function, called on each error (for example when doing retry operations) and can be used for logging or any other notification
- onFailureRespond((error,future)-> future.complete("")) : provides a terminal function, that will be called when no other retries or error handling is possible and defines the final response when the default execution fails.
- timeout(long ms) : define a timeout for creating the response, when the timeout is reached, the error handling will be executed (onError, onFailureResponde)
- retry(int amount) : define the amount of retries before response creation fails. After each (timeout)error the *onError* method will be executed (if defined). When no retries left, the onFailureResponse method will be executed (if defined)
- delay(long ms): works only in **blocking** mode and defines the amount of time between retries, in case of errors

### general error and exception propagation ###
Exceptions within the fluent API will be handled by methods like *onError* and *onFailureResponse*. If no error handling is defined, or the *onFailureResponse*
method is throwing an exception, it must be handled in the calling method (your REST method). In case you have an unhandled exception within your REST method, 
you can define a separate error-method to handle those exceptions (if no further error handling is defined , the response is delivering a http 500 response). 
To do so, define your error-handling method (with RestHandler and a Throwable as parameter) and annotate the method 
with the *@OnRestError* annotation. This annotation **must** contain the same REST-path as your corresponding @Path annotation. Example:

```java
    @Path("/helloGET/:name")
    @GET
    public void simpleREST(RestHandler handler) {

            handler.
                   response().stringResponse((future)->{
                   throw new NullPointerException("Test");
               }).onFailureRespond((throwable, future) -> {
                   throw new NullPointerException("Test");
               }).execute();
    }

    @OnRestError("/helloGET/:name")
    public void simpleRESTError(RestHandler handler, Throwable t) {
        // define a response in case of errors
    }
```

## circuit breaker
Vxms has a simple, built-in circuit breaker with two states: open and closed. Currently the circuit beaker is locking each Verticle instance separately. In further releases a locking on JVM Process & Cluster wide is planned. 
To activate the circuit breaker you need first to define a **retry(int amount)** amount on the fluent API, and than you can set the **closeCircuitBreaker(long ms)** time (the time before the circuit breaker will close again). 
Each request in-between this time will automatically execute the *onFailureResponse* method, 
without evaluating the *...response* method. Be aware, when you have N instances of your Verticle, each of them counts individually. 

## the event-bus bridge
The "RestHandler" has a build-in event-bus bridge. The idea is, that on each REST request, you can send a message via (Vert.x) event-bus, and map the response of you event-bus call to the original REST response (so you can create a chain). 
This way, you can easily build gateways to connect REST application with event-driven Vert.x (or Vxms) services (locally or in a cluster). The event-bus result-message can be mapped to String, Object or byte[] like any other response. 


```java
    @Path("/helloGET/:name")
    @GET
    public void simpleRESTEventbusChain(RestHandler handler) {

        handler.
            eventBusRequest().
            send("target.id", "message").
            stringResponse((result, future) -> {
              Message<Object> message = result.result();
              future.complete("hello " + message.body().toString());
            }).execute();
    }

```

