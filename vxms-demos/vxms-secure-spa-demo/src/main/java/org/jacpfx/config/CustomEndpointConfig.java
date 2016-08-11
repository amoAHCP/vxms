package org.jacpfx.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 22.04.16.
 */
public class CustomEndpointConfig implements EndpointConfiguration {


    @Override
    public void staticHandler(Router router) {
        router.route("/private/*").handler(StaticHandler.create());
    }

    public void sessionHandler(Vertx vertx, Router router) {
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    }

    @Override
    public void customRouteConfiguration(Vertx vertx, Router router, boolean secure, String host, int port) {
        OAuth2Auth authProvider = OAuth2Auth.create(vertx, OAuth2FlowType.AUTH_CODE, new OAuth2ClientOptions()
                .setClientID(vertx.getOrCreateContext().config().getString("clientID", "XXX"))
                .setClientSecret(vertx.getOrCreateContext().config().getString("clientSecret", "XXX"))
                .setSite("https://github.com/login")
                .setTokenPath("/oauth/access_token")
                .setAuthorizationPath("/oauth/authorize"));
        String hostURI = (secure ? "https://" : "http://") + host + ":" + port;
        OAuth2AuthHandler oauth2 = OAuth2AuthHandler.create(authProvider, hostURI);
        // setup the callback handler for receiving the GitHub callback, and authority to get access to basic user data
        oauth2.setupCallback(router.route("/callback")).addAuthority("user");
        // store authentication
        router.route().handler(UserSessionHandler.create(authProvider));
        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(oauth2);

        router.get("/").handler(ctx ->
                ctx.
                        response().
                        putHeader("content-type", "text/html").
                        end("Hello <br><a href=\"/private/\">Protected by Github</a>"));
    }

}
