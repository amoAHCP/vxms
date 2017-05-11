# vxms rest module
The vxms rest module allows the handling of rest request. To use the module, the VxmsEndpoint class must be extended (from the core module)
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
You can annotate any public method with Jax-RS annotations and add the "org.jacpfx.vertx.rest.response.RestHandler" class to the method signature to build the response. 
All methods must not have a return value, the response will be returned through the "RestHandler" API. **Each request must be ended, either by calling "execute()" or "end()"**, otherwise the http response will not be ended. 
The idea in vxms is, to model your response using the fluent API. Vxms provides a best practice of how to define a repose and to handle errors.

```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {
    @Path("/hello/:name")
    @GET
    public void simpleNonBlocking(RestHandler handler) {
       handler.
                 response().
                 stringResponse((response)->
	                 response.complete("simple response")).
                 execute();
		}
}
```
By default all your operations must be non-blocking. If you do blocking calls vxms behaves like typically Vert.x do. 
The "RestHandler" gives you the ability to create responses for following types: String, byte[], and Object. For an Object response you must specify an Encoder which maps your response Object to either String or byte[].
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
The vxms-rest module is using Jax-rs annotations. Following Annotations are supported:
- @Path : The Path string follows the Vertx conventions (like: /mypath/:id/:name)
- @GET, @POST, @OPTIONAL, @PUT, @DELETE
- @Consumes
- @Produces

## non-blocking rest response
Vert.x is per default a non-blocking framework, so in vxms non-blocking is the default behaviour. A response is always created using the "org.jacpfx.vertx.rest.response.RestHandler" and it's fluent API. 
When creating specific responses (like String, byte or Object), you need to call the *execute()* method, so the complete chain will be executed. Be aware, this is a non-blocking call!
For all non-blocking calls you must provide a consumer lambda, which gives you a future instance where you can complete the execution.
Following options you can use to define a response handling:
- handler.response().end() && handler.response().end(HttpStatus status) ends the request (if no response will be submitted)
- create a simple String response: handler.response().stringResponse((future)->future.complete("my string")).execute();
- create a simple byte response:  handler.response().byteResponse((future)->future.complete("my bytes".getBytes())).execute();
- create a simple object response:  handler.response().objectResponse((response)-> response.complete(new MyObject()),new MyEncoder()).execute();  ... since Object responses must be serialized you need to define a custom Encoder which serializes the Object either to String or to byte[]. The Encoder class must implement either  Encoder.StringEncoder<MyObject> or  Encoder.ByteEncoder<MyObject> 
- http specific configurations:
  - putHeader(key,val) : add custom headers to the response
  - execute(String contentType): set the content type of the response
  - execute(HttpResponseStatus status): set the http status for the response
## blocking rest response
The blocking API works quite similar to the non-blocking API in vxms. While using the non-blocking API you are restricted to the max execution times (time how long the event-loop can be blocked), 
whe using the blocking API, you are free to handle any long-running tasks. The basic difference in writing blocking responses is, that the mapping methods (mapToString, mapToObject) are using a supplier, which means you simply add a return value. To initialize the blocking API you must call *blocking()* on the response handler.

- handler.response().blocking() : initilize the blocking API
- create simple String response: handler.response().blocking().stringResponse(() ->"hello").execute();
- create simple byte response: handler.response().blocking().byteResponse(() ->"hello".getBytes()).execute();
- create simple Object response: handler.response().blocking().objectResponse(() ->new MyObject(),new MyEncoder()).execute();
## error handling
Vxms provides many features for handling errors while creating the REST response (in blocking and non-blocking mode):
- handler.response().onError((throwable) -> ....)... : this is an intermediate function, called on each error (for example when doing retry operations)
- handler.response().onFailureRespond((error,future)-> future.complete("")) : provides a treminal function, that will be called when no other retries or error handling is possible and defines the final response when the default one fails.
- timeout(long ms) : define a timeout for creating the response, when the timeout is reached the error handling will be executed (onError, onFailureResponde)
- retry(int amount) : define the amount of retries before response creation fails. After each error the onError method will be executed (if defined). When no retries left, the onFailureResponse method will be executed (if defined)
- delay(long ms): works only in **blocking** mode and defines the amount of time between retries in case of errors

### general error and exception propagation ###
Exceptions within the fluent API will be handled by methods like *onError* and *onFailureResponse*. If no error handling is defined, or the *onFailureResponse*
method is throwing an exception, it must be handled in the calling method (your REST method). In case you have an unhandled exception within your REST method you can define a separate error method to handle those exceptions (if no further error handling is defined , the response is delivering a http 500 response). To do so, define an error method (with org.jacpfx.vertx.rest.response.RestHandler and a Throwable as parameter) and annotate the method 
with the *@OnRestError* annotation. This annotation **must** contain the same REST path as your original REST method. Example:

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
Vxms has a simple built-in circuit breaker with two states: open and closed. Currently the circuit beaker is locking each Verticle instance separately. In further releases a locking on JVM Process & Cluster wide is planned. 
To activate the circuit breaker you need first to define a **retry(int amount)** amount on the fluent API and than you can set the **closeCircuitBreaker(long ms)** time before the circuit breaker will close again. Each request inbetween this time will automatically execute the *onFailureResponse* method, 
without evaluating the *mapTo...* method. Be aware, when you have N instances of your Verticle, each of them counts individually. 

## event-bus bridge
The "org.jacpfx.vertx.rest.response.RestHandler" has a build-in event-bus bridge. The idea is, that on each REST request, you can send a message via (Vert.x) event-bus and map the response to the original REST response. 
This way, you can easily build gateways to connect REST application with event-driven Vert.x (or vxms) services (locally or in a cluster). The event-bus result message can be mapped to String, Object or byte[] like any other response. 


```java
    @Path("/helloGET/:name")
    @GET
    public void simpleREST(RestHandler handler) {

        handler.
            eventBusRequest().
            send("target.id", "message").
            mapToStringResponse((result, future) -> {
              Message<Object> message = result.result();
              future.complete("hello " + message.body().toString());
            }).execute();
    }

```