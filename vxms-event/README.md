# vxms eventbus module
The vxms-eventbus module allows the handling of (Vert.x) eventbus messages. To use the module, the VxmsEndpoint class must be extended (from the core module)
```java
@ServiceEndpoint(port=8090)
public class RESTExample extends VxmsEndpoint {

}
```
 and following dependency added:

```xml   
  <dependency>
       <groupId>org.jacpfx</groupId>
       <artifactId>vxms-event</artifactId>
       <version>${version}</version>
  </dependency>
``` 

## basic usage
The vxms-eventbus module provides a non-blocking (with the option of blocking usage), fluent API to build a eventbus response. 
You can annotate any public method with the *org.jacpfx.vertx.event.annotation.Consume* annotation and add the *org.jacpfx.vertx.event.response.EventbusHandler* class to the method signature to build the response. 
All methods must not have a return value, the response will be returned through the "EventbusHandler" API. The idea in vxms is, to model your response using the fluent API. 
Vxms provides a best practice of how to define a repose and to handle errors.

```java
@ServiceEndpoint
public class EventbusExample extends VxmsEndpoint {
    @Consume("simple.event")
    public void simpleNonBlocking(EventbusHandler handler) {
       handler.
                 response().
                 stringResponse(response->
	                 response.complete("simple response")).
                 execute();
		}
}
```