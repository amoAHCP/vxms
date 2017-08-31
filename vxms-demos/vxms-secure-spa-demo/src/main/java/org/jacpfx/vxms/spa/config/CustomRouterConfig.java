/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.spa.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;

/**
 * Created by Andy Moncsek on 22.04.16.
 */
public class CustomRouterConfig implements RouterConfiguration {


    @Override
    public void staticHandler(Router router) {
        router.route("/private/*").handler(StaticHandler.create());
    }

    public void sessionHandler(Vertx vertx, Router router) {
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    }



    @Override
    public void customRouteConfiguration(Vertx vertx, Router router, boolean secure, String host, int port) {
        final JsonObject config = vertx.getOrCreateContext().config();
        final OAuth2Auth authProvider = GithubAuth
            .create(vertx,config.getString("clientID"),config.getString("clientSecret"));
        // store authentication
        router.route().handler(UserSessionHandler.create(authProvider));

        final String hostURI = (secure ? "https://" : "http://") + host + ":" + port;
        final String callbackURI = hostURI+"/callback";

        final OAuth2AuthHandler oauth2 = OAuth2AuthHandler.create(authProvider, callbackURI);

        // setup the callback handler for receiving the GitHub callback
        oauth2.setupCallback(router.route());

        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(oauth2);

        router.get("/").handler(ctx ->
                ctx.
                        response().
                        putHeader("content-type", "text/html").
                        end("Hello <br><a href=\"/private/\">Protected by Github</a>"));
    }

}
