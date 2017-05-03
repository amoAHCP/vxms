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

## circuit breaker

## event bus bridge