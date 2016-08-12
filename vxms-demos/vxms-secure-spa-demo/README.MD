# vxms-secure-spa-demo
This demo show a small SPA application (AngularJS) using six REST endpoints. The demo uses ssl to secure the content and OAuth2 to authenticate the user and protect the access to the REST endpoints. 

## pre requirements

This Demo ist using Oaut2 authentication with Github, therefore you need to register an OAuth application in your developer area at Github. To do so, go to https://github.com/settings/developers --> *OAuth applications* --> *Developer Applications* --> *Register new application* , add the homepage URL (*https://localhost:8443*) and callback URL (*https://localhost:8443/callback*). Add the generated *Client ID* and *Client Secret* to the *securityConf.json* or in the *main* method of the *org.jacpfx.verticle.SecureEndpoint*" class.


## start the application
Either go to *org.jacpfx.verticle.SecureEndpoint* class and use the *main* method, or build the project (*mvn clean package*) and run *java -jar target/vxms-secure-spa-demo-fat.jar*

## use the application
Go to *https://localhost:8443*, since we use a self signed certificate (created on first start), the browser will show you some alert messages. After you signed in with you Github account you will be redirected to *https://localhost:8443/private*. The whole UI and all the REST endpoints are now protected by OAuth2.

## The SecureEndpoint
The *org.jacpfx.verticle.SecureEndpoint* class is a vxms endpoint using the vxms-rest extension and the *vertx-spring-ext* extension (*@SpringVerticle*). We use spring here (for demo purposes) to inject the local user repository. The vxms endpoint is defining six REST endpoints and serves the static content.

## CustomHTTPOptions
The *CustomHTTPOptions* class is defining the ssl context for the endpoint. For demo purposes we define a self signed certificate (if not exists) and set the values to *io.vertx.core.http.HttpServerOptions*. Keep in mind, that we use plain Vert.x under the hood, so all configurations you can do in Vert.x you can also do here. To register the *CustomHTTPOptions* we add the reference to the *@ServiceEndpoint(...options = CustomHTTPOptions.class)* annotation located in the vxms endpoint (*SecureEndpoint*).
## CustomEndpointConfig

With the *CustomEndpointConfig* class with customize several aspects of the vxms endpoint. First of all the default methods of the interfaces *EndpointConfiguration* (which is implemented by the *CustomEndpointConfig* class ), already defines a body- and a cookie handler. In the *CustomEndpointConfig* class we furthermore define the static content handler (to serve the AngularJS content under */private* url), and we use the *customRouteConfiguration* method to define all OAuth2 related initialisations. Again, keep in mind that all those configurations are also valid in a plain Vert.x environment.
