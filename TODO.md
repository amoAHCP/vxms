# Todos 24. Okt

- org.jacpfx.vxms.event.response.basic.Stepexecution && org.jacpfx.vxms.rest.base.response.basic.Stepexecution
    - try to unify classes or find a better abstraction
    
- https://vertx.io/docs/apidocs/io/vertx/core/http/HttpServerResponse.html#isChunked--
    - add more method facades to response
    
- remove vxms-rest-rs
    - migrate all U_nit tests befor
    
    
    
    
    
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-core/src/main/java/org/jacpfx/vxms/services/VxmsEndpoint.java
    Warning:(174, 31) java: accept(io.vertx.core.http.HttpServerRequest) in io.vertx.ext.web.Router has been deprecated
    Warning:(179, 28) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    Warning:(256, 23) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(282, 24) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(303, 20) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-core/src/main/java/org/jacpfx/vxms/common/configuration/RouterConfiguration.java
    Warning:(54, 28) java: io.vertx.ext.web.handler.CookieHandler in io.vertx.ext.web.handler has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-core/src/main/java/org/jacpfx/vxms/common/util/ReflectionExecutionWrapper.java
    Warning:(107, 18) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/response/RESTRequest.java
    Warning:(82, 14) java: io.vertx.ext.web.Cookie in io.vertx.ext.web has been deprecated
    Warning:(83, 19) java: cookies() in io.vertx.ext.web.RoutingContext has been deprecated
    Warning:(92, 10) java: io.vertx.ext.web.Cookie in io.vertx.ext.web has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/eventbus/basic/EventbusRequest.java
    Warning:(110, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/response/basic/StepExecution.java
    Warning:(459, 49) java: <T>future() in io.vertx.core.Future has been deprecated
    Warning:(521, 24) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/eventbus/basic/EventbusExecution.java
    Warning:(333, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/eventbus/blocking/EventbusExecution.java
    Warning:(344, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-rest-base/src/main/java/org/jacpfx/vxms/rest/base/response/basic/ResponseExecution.java
    Warning:(438, 43) java: <T>future() in io.vertx.core.Future has been deprecated
    Warning:(498, 18) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-k8sdiscovery/src/main/java/org/jacpfx/vxms/k8s/client/VxmsDiscoveryK8SImpl.java
    Warning:(83, 30) java: newInstance() in java.lang.Class has been deprecated
    Information:java: /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-k8sdiscovery/src/main/java/org/jacpfx/vxms/k8s/client/VxmsDiscoveryK8SImpl.java uses unchecked or unsafe operations.
    Information:java: Recompile with -Xlint:unchecked for details.
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/response/basic/StepExecution.java
    Warning:(454, 49) java: <T>future() in io.vertx.core.Future has been deprecated
    Warning:(516, 24) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/eventbus/basic/EventbusBridgeRequest.java
    Warning:(115, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/response/basic/ResponseExecution.java
    Warning:(434, 43) java: <T>future() in io.vertx.core.Future has been deprecated
    Warning:(494, 18) java: fail(java.lang.Throwable) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/eventbus/basic/EventbusBridgeExecution.java
    Warning:(299, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/eventbus/blocking/EventbusBridgeExecution.java
    Warning:(306, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.eventbus.DeliveryOptions,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/response/EventbusRequest.java
    Information:java: /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-event/src/main/java/org/jacpfx/vxms/event/response/EventbusRequest.java uses unchecked or unsafe operations.
    Information:java: Recompile with -Xlint:unchecked for details.
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/rest/RESTJerseyClientCookieTest.java
    Warning:(26, 24) java: io.vertx.ext.web.Cookie in io.vertx.ext.web has been deprecated
    Warning:(120, 13) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(151, 7) java: io.vertx.ext.web.Cookie in io.vertx.ext.web has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/rest/RESTJerseyClientStaticTest.java
    Warning:(103, 13) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/evbbridge/RESTJerseyClientEventByteCircuitBreakerTest.java
    Warning:(142, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(240, 81) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(213, 57) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(190, 33) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(173, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(319, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(344, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(362, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(386, 18) java: complete() in io.vertx.core.Future has been deprecated
    Information:java: Some input files use unchecked or unsafe operations.
    Information:java: Recompile with -Xlint:unchecked for details.
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/verticle/Testverticle.java
    Warning:(50, 30) java: accept(io.vertx.core.http.HttpServerRequest) in io.vertx.ext.web.Router has been deprecated
    Warning:(51, 16) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/evbbridge/RESTJerseyClientEventObjectResponseAsyncTest.java
    Warning:(144, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(175, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(206, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(236, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(266, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(386, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(410, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/postconstruct/PostConstructTest.java
    Warning:(124, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(146, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(169, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(186, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/kuberenetes/ResolveServicesByLabelWithConfigOKTest.java
    Warning:(169, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/evbbridge/RESTJerseyClientEventStringResponseTest.java
    Warning:(140, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(165, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(190, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(217, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(242, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(267, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(312, 58) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(333, 40) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(395, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(419, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/rest/RESTServiceSelfhostedAsyncTestStaticInitializer.java
    Warning:(106, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(128, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/failure/RESTServiceOnFailureByteResponseTest.java
    Warning:(109, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(146, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(180, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(220, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(291, 23) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(309, 23) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(329, 23) java: complete(T) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/kuberenetes/ResolveServicesByNameOfflineTest.java
    Warning:(110, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(150, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/chain/EventbusBlockingChainingStringTest.java
    Warning:(99, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(114, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(129, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(144, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(159, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(174, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(189, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(243, 51) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(227, 35) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(213, 19) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(204, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(308, 51) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(292, 35) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(278, 19) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(269, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/eventbus/EventbusFailureTests.java
    Warning:(103, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(119, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(135, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(151, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(177, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(203, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(229, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(255, 9) java: <T>send(java.lang.String,java.lang.Object,io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.eventbus.Message<T>>>) in io.vertx.core.eventbus.EventBus has been deprecated
    Warning:(294, 40) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(312, 40) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(345, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(365, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(396, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(416, 26) java: complete(T) in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/evbbridge/RESTJerseyClientEventStringCircuitBreakerTest.java
    Warning:(139, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(203, 81) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(185, 57) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(171, 33) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(161, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(258, 40) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(281, 40) java: complete(T) in io.vertx.core.Future has been deprecated
    Warning:(299, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(323, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/rest/RESTJerseyClientSessionTest.java
    Warning:(107, 13) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/evbbridge/RESTJerseyClientEventStringCircuitBreakerAsyncTest.java
    Warning:(140, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(202, 81) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(184, 57) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(171, 33) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(162, 15) java: get(java.lang.String,io.vertx.core.Handler<io.vertx.core.http.HttpClientResponse>) in io.vertx.core.http.HttpClient has been deprecated
    Warning:(302, 18) java: complete() in io.vertx.core.Future has been deprecated
    Warning:(326, 18) java: complete() in io.vertx.core.Future has been deprecated
    /Users/andy.moncsek/Documents/development/vertx/vxms/vxms-testing/src/test/java/org/jacpfx/kuberenetes/ResolveServicesByLabelWithConfigNOKTest.java
    Information:java: Some input files additionally use or override a deprecated API.