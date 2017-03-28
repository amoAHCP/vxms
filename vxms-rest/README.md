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
The vxms-rest module provides a non-blocking (with the option of blocking usage), fluent API to build a REST response. You can annotate a method with Jax-RS annotations and add the "RestHandler" class to the method signature to build the response. All methods must not have a return value, the response will be returned through the "RestHandler" API. **Each request must be ended, either by calling "execute()" or "end()"**, otherwise the http response will not be ended.

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

## non-blocking rest response

## blocking rest response

## error handling

## circuit breaker

## event bus bridge